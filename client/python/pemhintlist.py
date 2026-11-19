#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import base64
import sys
import typing

import requests
from cryptography import x509
from cryptography.hazmat.primitives.serialization import Encoding


def base64_to_pem(base64_str: str) -> str:
    der_bytes = base64.b64decode(base64_str)
    certificate = x509.load_der_x509_certificate(der_bytes)
    return certificate.public_bytes(Encoding.PEM).decode("ascii")


def intermediates(host: str, policy: str, timeout: int = 30) -> typing.List[typing.Dict[str, typing.Any]]:
    intermediates_uri = f"{host.rstrip('/')}/vss/v2/intermediates/{policy}"
    response = requests.get(intermediates_uri, timeout=timeout)
    response.raise_for_status()
    return response.json()


def policies(host: str, policy: str, timeout: int = 30) -> typing.Dict[str, typing.Any]:
    policies_uri = f"{host.rstrip('/')}/vss/v2/policies/{policy}"
    response = requests.get(policies_uri, timeout=timeout)
    response.raise_for_status()
    return response.json()


def main(argv: typing.Optional[typing.Sequence[str]] = None) -> int:
    args = list(sys.argv[1:] if argv is None else argv)
    if len(args) != 2:
        print("Usage: pemhintlist.py <host> <policy>", file=sys.stderr)
        return 2
    host, policy = args
    try:
        # Print trust anchors first, followed by intermediate CA certificates in PEM format.
        policy_json = policies(host, policy)
        for cert_entry in policy_json.get("trustAnchors", []):
            print(base64_to_pem(cert_entry["x509Certificate"]), end="")

        intermediate_json = intermediates(host, policy)
        for cert_entry in intermediate_json:
            print(base64_to_pem(cert_entry["x509Certificate"]), end="")
    except Exception as exc:
        print(f"Error building PEM hint list: {exc}", file=sys.stderr)
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main())
