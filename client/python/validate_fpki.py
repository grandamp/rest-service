#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import argparse
import base64
import datetime
import hashlib
import sys
import typing

import requests
from cryptography import x509
from cryptography.hazmat.primitives.serialization import Encoding, pkcs7

P7B_URL = "https://www.idmanagement.gov/implement/tools/CACertificatesValidatingToFederalCommonPolicyG2.p7b"
VSS_HOST = "https://home.keysupport.org"
VSS_POLICY = "1.3.6.1.5.5.7.19.1"


def cert_to_base64(cert: x509.Certificate) -> str:
    return base64.b64encode(cert.public_bytes(Encoding.DER)).decode("ascii")


def cert_x5t_s256(cert: x509.Certificate) -> str:
    digest = hashlib.sha256(cert.public_bytes(Encoding.DER)).digest()
    return base64.urlsafe_b64encode(digest).decode("ascii").rstrip("=")


def validate_cert(
    cert: x509.Certificate,
    endpoint: str = f"{VSS_HOST}/vss/v2/validate",
    policy: str = VSS_POLICY,
    *,
    verify: bool = True,
    timeout: int = 10,
) -> typing.Dict[str, typing.Any]:
    vss_cert = cert_to_base64(cert)
    expected_x5t = cert_x5t_s256(cert)
    request_data = {
        "validationPolicyId": policy,
        "x509Certificate": vss_cert,
        "metaData": {"x5t#S256": expected_x5t},
    }

    try:
        response = requests.post(
            endpoint,
            json=request_data,
            verify=verify,
            timeout=timeout,
        )
        response.raise_for_status()
        res_json = response.json()
    except Exception as exc:
        return {"error": str(exc)}

    resp_x5t = res_json.get("x5t#S256")
    if not isinstance(resp_x5t, str) or resp_x5t.rstrip("=") != expected_x5t:
        return {
            "error": (
                f"Requested cert digest ({expected_x5t}) does not match "
                f"response digest ({resp_x5t})"
            )
        }
    return res_json


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Validate all certificates in the upstream FPKI P7B bundle against VSS."
    )
    parser.add_argument("--host", default=VSS_HOST, help=f"Base VSS URL (default: {VSS_HOST})")
    parser.add_argument("--policy", default=VSS_POLICY, help=f"Validation policy (default: {VSS_POLICY})")
    parser.add_argument("--p7b-url", default=P7B_URL, help="URL to FPKI P7B bundle")
    parser.add_argument(
        "-k",
        "--insecure",
        action="store_true",
        help="Disable TLS certificate verification (not recommended)",
    )
    return parser


def main(argv: typing.Optional[typing.Sequence[str]] = None) -> int:
    parser = build_parser()
    try:
        args = parser.parse_args(argv)
    except SystemExit as exc:
        return 2 if exc.code != 0 else 0

    verify_tls = not args.insecure
    vss_endpoint = f"{args.host.rstrip('/')}/vss/v2/validate"

    print(f"Fetching P7B from {args.p7b_url} ...")
    try:
        res = requests.get(args.p7b_url, verify=verify_tls, timeout=30)
        res.raise_for_status()
        p7b_bytes = res.content
    except Exception as exc:
        print(f"Failed to fetch P7B: {exc}", file=sys.stderr)
        return 2

    print("Parsing certificates...")
    try:
        certs = pkcs7.load_der_pkcs7_certificates(p7b_bytes)
    except Exception as exc:
        try:
            certs = pkcs7.load_pem_pkcs7_certificates(p7b_bytes)
        except Exception as exc2:
            print(f"Failed to parse P7B as DER ({exc}) or PEM ({exc2})", file=sys.stderr)
            return 2

    print(f"Found {len(certs)} certificates. Validating against {vss_endpoint} (policy: {args.policy})...")

    valid_certs: typing.List[str] = []
    invalid_certs: typing.List[typing.Tuple[str, str]] = []
    error_certs: typing.List[typing.Tuple[str, str]] = []

    for i, cert in enumerate(certs):
        print(f"Validating cert {i+1}/{len(certs)}...", end="\r")
        result = validate_cert(cert, vss_endpoint, args.policy, verify=verify_tls)

        subject = "Unknown"
        try:
            subject_attr = cert.subject.get_attributes_for_oid(x509.NameOID.COMMON_NAME)
            if subject_attr:
                subject = str(subject_attr[0].value)
            else:
                subject = cert.subject.rfc4514_string()
        except Exception:
            pass

        if "error" in result:
            error_certs.append((subject, str(result["error"])))
            continue

        validation_res = result.get("validationResult", {})
        status = validation_res.get("result", "UNKNOWN")

        if status == "SUCCESS":
            valid_certs.append(subject)
        else:
            reason = str(validation_res.get("invalidityReasonText", "Unknown reason"))
            invalid_certs.append((subject, reason))

    print(" " * 50, end="\r")

    print("=" * 60)
    print(f"FPKI ANALYSIS REPORT - {datetime.datetime.now(datetime.timezone.utc).isoformat()}")
    print("=" * 60)

    print(f"\n--- VALID CERTIFICATES ({len(valid_certs)}) ---")
    for subj in valid_certs:
        print(f" - {subj}")

    print(f"\n--- INVALID CERTIFICATES ({len(invalid_certs)}) ---")
    for subj, reason in invalid_certs:
        print(f" - {subj}")
        print(f"   Reason: {reason}")

    if error_certs:
        print(f"\n--- VALIDATION ERRORS ({len(error_certs)}) ---")
        for subj, err in error_certs:
            print(f" - {subj}")
            print(f"   Error: {err}")

    if error_certs:
        return 2
    if invalid_certs:
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
