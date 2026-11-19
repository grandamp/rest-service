#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import argparse
import base64
import enum
import hashlib
import json
import sys
import typing

import requests
from cryptography import x509
from cryptography.hazmat.primitives.serialization import Encoding, pkcs7
from cryptography.x509.oid import ExtensionOID


class FileEncoding(enum.Enum):
    PEM = "PEM"
    DER = "DER"
    PEMPKCS7 = "PEMPKCS7"
    DERPKCS7 = "DERPKCS7"


def cert_to_base64(cert: x509.Certificate) -> str:
    return base64.b64encode(cert.public_bytes(Encoding.DER)).decode("ascii")


def cert_x5t_s256(cert: x509.Certificate) -> str:
    digest = hashlib.sha256(cert.public_bytes(Encoding.DER)).digest()
    return base64.urlsafe_b64encode(digest).decode("ascii").rstrip("=")


def decode_b64url(value: str) -> bytes:
    padded = value + "=" * (-len(value) % 4)
    return base64.urlsafe_b64decode(padded)


def loadCerts(file: str, file_type: FileEncoding) -> typing.List[x509.Certificate]:
    with open(file, "rb") as handle:
        data = handle.read()
    if file_type == FileEncoding.DERPKCS7:
        return pkcs7.load_der_pkcs7_certificates(data)
    if file_type == FileEncoding.PEMPKCS7:
        return pkcs7.load_pem_pkcs7_certificates(data)
    if file_type == FileEncoding.PEM:
        return x509.load_pem_x509_certificates(data)
    if file_type == FileEncoding.DER:
        return [x509.load_der_x509_certificate(data)]
    raise ValueError(f"Unsupported file encoding: {file_type}")


def load_certificates(file_path: str) -> typing.List[x509.Certificate]:
    """Load one or more X.509 certificates by content (PEM, PEM PKCS#7, DER, or DER PKCS#7)."""
    with open(file_path, "rb") as handle:
        data = handle.read()

    if not data:
        raise ValueError(f"Certificate file is empty: {file_path}")

    if b"-----BEGIN PKCS7-----" in data or b"-----BEGIN CMS-----" in data:
        return pkcs7.load_pem_pkcs7_certificates(data)
    if b"-----BEGIN CERTIFICATE-----" in data:
        return x509.load_pem_x509_certificates(data)

    try:
        return [x509.load_der_x509_certificate(data)]
    except ValueError:
        return pkcs7.load_der_pkcs7_certificates(data)


def validate(
    host: str,
    policy: str,
    vss_cert: str,
    expected_x5t_s256: typing.Optional[str] = None,
    *,
    verify: bool = True,
    timeout: int = 30,
    quiet: bool = False,
) -> typing.Dict[str, typing.Any]:
    request_payload: typing.Dict[str, typing.Any] = {
        "validationPolicyId": policy,
        "x509Certificate": vss_cert,
    }
    if expected_x5t_s256 is not None:
        request_payload["metaData"] = {"x5t#S256": expected_x5t_s256}

    api_endpoint_uri = f"{host.rstrip('/')}/vss/v2/validate"
    if not quiet:
        print("\nRequest:\n" + json.dumps(request_payload, sort_keys=False, indent=4))

    response = requests.post(
        api_endpoint_uri,
        json=request_payload,
        verify=verify,
        timeout=timeout,
    )
    if response.status_code < 200 or response.status_code >= 300:
        raise RuntimeError(
            f"Unexpected HTTP {response.status_code} from {api_endpoint_uri}: {response.text}"
        )

    try:
        res_json = response.json()
    except ValueError as exc:
        raise RuntimeError(
            f"Invalid JSON response from {api_endpoint_uri}: {response.text}"
        ) from exc

    if not quiet:
        print("\nResponse:\n" + json.dumps(res_json, sort_keys=False, indent=4))

    if expected_x5t_s256 is not None:
        resp_x5t = res_json.get("x5t#S256")
        if not isinstance(resp_x5t, str) or resp_x5t.rstrip("=") != expected_x5t_s256:
            raise ValueError(
                f"Requested cert digest ({expected_x5t_s256}) does not match "
                f"response digest ({resp_x5t})"
            )

    return res_json


def validateCert(
    certificate: x509.Certificate,
    host: str,
    policy: str,
    *,
    verify: bool = True,
    timeout: int = 30,
    quiet: bool = False,
) -> bool:
    vsscert = cert_to_base64(certificate)
    expected_x5t = cert_x5t_s256(certificate)
    res_json = validate(
        host,
        policy,
        vsscert,
        expected_x5t,
        verify=verify,
        timeout=timeout,
        quiet=quiet,
    )

    print("Issuer: " + str(res_json.get("x509IssuerName", "")))
    print("Subject: " + str(res_json.get("x509SubjectName", "")))
    sha256_thumbprint = res_json["x5t#S256"]
    sha256_thumbprint_hex = decode_b64url(sha256_thumbprint).hex()
    print("Thumbprint (SHA-256): " + sha256_thumbprint_hex)

    try:
        ski = certificate.extensions.get_extension_for_oid(
            ExtensionOID.SUBJECT_KEY_IDENTIFIER
        )
        print("kid: " + ski.value.key_identifier.hex())
    except x509.ExtensionNotFound:
        pass

    validation_result = res_json.get("validationResult") or {}
    cert_result = validation_result.get("result", "UNKNOWN")
    print("Validation Result: " + cert_result)
    if cert_result == "SUCCESS":
        if not quiet:
            print(
                "Valid Certificate:\n"
                + certificate.public_bytes(Encoding.PEM).decode("ascii")
            )
        return True

    if cert_result == "FAIL":
        print(
            "\tValidation Failure Reason: "
            + str(validation_result.get("invalidityReasonText", "Unknown"))
        )
        print(
            "\tValidation Affirmed Invalid: %s"
            % validation_result.get("isAffirmativelyInvalid")
        )
    return False


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Validate X.509 certificates (.pem, .der, .cer, .crt, .p7b, .p7c) against a VSS v2 service."
    )
    parser.add_argument("cert_file", help="Path to certificate or PKCS#7 bundle")
    parser.add_argument("host", help="Base VSS service URL, e.g. https://keysupport.net")
    parser.add_argument("policy", help="Validation policy OID or ID, e.g. 1.3.6.1.5.5.7.19.1")
    parser.add_argument(
        "-q",
        "--quiet",
        action="store_true",
        help="Suppress verbose request/response JSON and PEM dumps",
    )
    parser.add_argument(
        "-k",
        "--insecure",
        action="store_true",
        help="Disable TLS server certificate verification (not recommended)",
    )
    return parser


def main(argv: typing.Optional[typing.Sequence[str]] = None) -> int:
    parser = build_parser()
    try:
        args = parser.parse_args(argv)
    except SystemExit as exc:
        return 2 if exc.code != 0 else 0

    try:
        certs = load_certificates(args.cert_file)
    except Exception as exc:
        print(f"Error loading certificates from {args.cert_file}: {exc}", file=sys.stderr)
        return 2

    if not certs:
        print(f"No X.509 certificates found in {args.cert_file}", file=sys.stderr)
        return 2

    all_valid = True
    for certificate in certs:
        try:
            ok = validateCert(
                certificate,
                args.host,
                args.policy,
                verify=not args.insecure,
                quiet=args.quiet,
            )
            if not ok:
                all_valid = False
        except Exception as exc:
            print(f"Validation error: {exc}", file=sys.stderr)
            return 2

    return 0 if all_valid else 1


if __name__ == "__main__":
    sys.exit(main())
