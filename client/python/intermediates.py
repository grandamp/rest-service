#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import sys
import typing

import requests


def intermediates(host: str, policy: str, timeout: int = 30) -> typing.List[typing.Dict[str, typing.Any]]:
    api_endpoint_uri = f"{host.rstrip('/')}/vss/v2/intermediates/{policy}"
    response = requests.get(api_endpoint_uri, timeout=timeout)
    response.raise_for_status()
    return response.json()


def printPEM(base64str: str) -> None:
    print("-----BEGIN CERTIFICATE-----")
    for i in range(0, len(base64str), 64):
        print(base64str[i:i + 64])
    print("-----END CERTIFICATE-----")


def main(argv: typing.Optional[typing.Sequence[str]] = None) -> int:
    args = list(sys.argv[1:] if argv is None else argv)
    if len(args) != 2:
        print("Usage: intermediates.py <host> <policy>", file=sys.stderr)
        return 2
    host, policy = args
    try:
        res_json = intermediates(host, policy)
    except Exception as exc:
        print(f"Error fetching intermediates: {exc}", file=sys.stderr)
        return 2
    for cert_entry in res_json:
        printPEM(cert_entry["x509Certificate"])
    return 0


if __name__ == "__main__":
    sys.exit(main())
