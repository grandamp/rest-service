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

def printPEM(base64str: str) -> str:
    line = []
    # Add PEM header
    line.append("-----BEGIN CERTIFICATE-----")
    # Add Base64 DER string values, split at 64 chars
    for i in range(0, len(base64str), 64):
        line.append(base64str[i:i+64])
    # Add PEM footer
    line.append("-----END CERTIFICATE-----")
    for x in line:
        print(x)

if __name__ == "__main__":
    host = sys.argv[1]
    policy = sys.argv[2]
    resJson = intermediates(host, policy)
    for x509Certificate in resJson:
        printPEM(x509Certificate['x509Certificate'])
