#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import json
import sys
import typing

import requests


def policies(host: str, policy: str, timeout: int = 30) -> typing.Dict[str, typing.Any]:
    api_endpoint_uri = f"{host.rstrip('/')}/vss/v2/policies/{policy}"
    response = requests.get(api_endpoint_uri, timeout=timeout)
    response.raise_for_status()
    return response.json()


def main(argv: typing.Optional[typing.Sequence[str]] = None) -> int:
    args = list(sys.argv[1:] if argv is None else argv)
    if len(args) != 2:
        print("Usage: policies.py <host> <policy>", file=sys.stderr)
        return 2
    host, policy = args
    try:
        res_json = policies(host, policy)
    except Exception as exc:
        print(f"Error fetching policy: {exc}", file=sys.stderr)
        return 2
    print(json.dumps(res_json, sort_keys=False, indent=4))
    return 0


if __name__ == "__main__":
    sys.exit(main())
