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

def base64_to_pem(base64Str: str) -> str:
    derBytes = base64.b64decode(base64Str)
    certificate = x509.load_der_x509_certificate(derBytes)
    return certificate.public_bytes(Encoding.PEM).decode("utf-8")

def intermediates(host: str, policy: str) -> typing.Dict:
    intermediatesUri = host + "/vss/v2/intermediates/" + policy
    response = requests.get(intermediatesUri)
    return response.json()

def policies(host: str, policy: str) -> typing.Dict:
    policiesUri = host + "/vss/v2/policies/" + policy
    response = requests.get(policiesUri)
    return response.json()

if __name__ == "__main__":
    host = sys.argv[1]
    policy = sys.argv[2]
    #TODO:  Get get truse anchors first, and print them in PEM
    policyJson = policies(host, policy)
    trustAnchors = policyJson["trustAnchors"]
    for x509Certificate in trustAnchors:
        print(base64_to_pem(x509Certificate['x509Certificate']))
    
    intermediateJson = intermediates(host, policy)
    for x509Certificate in intermediateJson:
        print(base64_to_pem(x509Certificate['x509Certificate']))

