#!/usr/bin/python3
# -*- coding: utf-8 -*-
import base64
import sys
import requests
import json
import typing
import cryptography

from cryptography import x509 
from cryptography.hazmat.primitives.serialization import pkcs7, Encoding
from cryptography.x509.oid import ExtensionOID
from cryptography import utils

class FileEncoding(utils.Enum):
    PEM = "PEM"
    DER = "DER"
    PEMPKCS7 = "PEMPKCS7"
    DERPKCS7 = "DERPKCS7"

def cert_to_base64(cert: x509.Certificate) -> str:
    return base64.b64encode(cert.public_bytes(Encoding.DER)).decode("utf-8")

def loadCerts(file: str, type: FileEncoding) -> typing.List[x509.Certificate]:
    bytes = open(file, 'rb').read()
    if type == FileEncoding.DERPKCS7 :
        return pkcs7.load_der_pkcs7_certificates(bytes)
    if type == FileEncoding.PEMPKCS7:
        return pkcs7.load_pem_pkcs7_certificates(bytes)
    if type == FileEncoding.PEM:
        return x509.load_pem_x509_certificates(bytes)
    if type == FileEncoding.DER:
        return x509.load_der_x509_certificate(bytes)

def validate(host: str, policy: str, vssCert:str) -> typing.Dict:
    request = {"validationPolicyId":policy,"x509Certificate":vssCert}
    apiEndpointUri = host + "/vss/v2/validate"
    print("\nRequest:\n" + json.dumps(request, sort_keys=False, indent=4))
    response = requests.post(apiEndpointUri, json=request)
    resJson = response.json()
    print("\nResponse:\n" + json.dumps(resJson, sort_keys=False, indent=4))
    return resJson

def validateCert(certificate):
    vsscert = cert_to_base64(certificate)
    resJson = validate(host, policy, vsscert)
    x509IssuerName = resJson["x509IssuerName"]
    print("Issuer: " + x509IssuerName)
    x509SubjectName = resJson["x509SubjectName"]
    print("Subject: " + x509SubjectName)
    sha256Thumbprint = resJson["x5t#S256"]
    sha256ThumbprintHex = base64.b64decode(sha256Thumbprint).hex()
    print("Thumbprint (SHA-256): " + sha256ThumbprintHex)
    # TODO: Don't assume the certificate has a subjectKeyIdentifier value.  Use a Try statement.
#    ski = certificate.extensions.get_extension_for_oid(ExtensionOID.SUBJECT_KEY_IDENTIFIER)
#    kid = ski.value.key_identifier.hex()
#   print("kid: " + kid)
    certResult = resJson["validationResult"]["result"]
    print("Validation Result: " + certResult)
    if certResult == "FAIL":
        invalidityReasonText = resJson["validationResult"]["invalidityReasonText"]
        print("\tValidation Failure Reason: " + invalidityReasonText)
        isAffirmativelyInvalid = resJson["validationResult"]["isAffirmativelyInvalid"]
        print("\tValidation Affirmed Invalid: %s" %  isAffirmativelyInvalid)
    else:
        print("Valid Certificate:\n" + certificate.public_bytes(Encoding.PEM).decode("utf-8"))

if __name__ == "__main__":
    filename = sys.argv[1]
    host = sys.argv[2]
    policy = sys.argv[3]
    if (filename.endswith('p7c') | filename.endswith('p7b') | filename.endswith('p7s') | filename.endswith('p7m')):
        certs = loadCerts(filename, FileEncoding.DERPKCS7)
        for certificate in certs:
            validateCert(certificate)
    if filename.endswith('pem'):
        certs = loadCerts(filename, FileEncoding.PEM)
        for certificate in certs:
            validateCert(certificate)
    if (filename.endswith('cer') | filename.endswith('crt') | filename.endswith('der')):
        certs = loadCerts(filename, FileEncoding.DER)
        for certificate in certs:
            validateCert(certificate)
