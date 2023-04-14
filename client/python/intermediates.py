#!/usr/bin/python3
# -*- coding: utf-8 -*-
import base64
import sys
import requests
import json
import typing
import cryptography

from cryptography import x509 
from cryptography.hazmat.primitives.serialization import Encoding

def cert_to_base64(cert: x509.Certificate) -> str:
    return base64.b64encode(cert.public_bytes(Encoding.DER)).decode("utf-8")

def intermediates(host: str, policy: str) -> typing.Dict:
    apiEndpointUri = host + "/vss/v2/intermediates/" + policy
    response = requests.get(apiEndpointUri)
    return response.json()

if __name__ == "__main__":
    host = sys.argv[1]
    policy = sys.argv[2]
    resJson = intermediates(host, policy)
    print("\nResponse:\n" + json.dumps(resJson, sort_keys=False, indent=4))
