#!/usr/bin/env python3
import base64
import requests
import json
import urllib3
import typing
import cryptography
import datetime
from cryptography.hazmat.primitives.serialization import pkcs7, Encoding
from cryptography import x509

urllib3.disable_warnings()

P7B_URL = "https://www.idmanagement.gov/implement/tools/CACertificatesValidatingToFederalCommonPolicyG2.p7b"
VSS_HOST = "https://home.keysupport.org"
VSS_POLICY = "1.3.6.1.5.5.7.19.1"
VSS_ENDPOINT = f"{VSS_HOST}/vss/v2/validate"

def cert_to_base64(cert: x509.Certificate) -> str:
    return base64.b64encode(cert.public_bytes(Encoding.DER)).decode("utf-8")

def validate_cert(cert: x509.Certificate) -> typing.Dict:
    vss_cert = cert_to_base64(cert)
    request_data = {
        "validationPolicyId": VSS_POLICY,
        "x509Certificate": vss_cert
    }
    
    try:
        response = requests.post(VSS_ENDPOINT, json=request_data, verify=False, timeout=10)
        response.raise_for_status()
        return response.json()
    except Exception as e:
        return {"error": str(e)}

def main() -> None:
    print(f"Fetching P7B from {P7B_URL} ...")
    try:
        res = requests.get(P7B_URL, timeout=30)
        res.raise_for_status()
        p7b_bytes = res.content
    except Exception as e:
        print(f"Failed to fetch P7B: {e}")
        return

    print("Parsing certificates...")
    try:
        certs = pkcs7.load_der_pkcs7_certificates(p7b_bytes)
    except Exception as e:
        print(f"Failed to parse P7B as DER: {e}")
        # Try PEM just in case
        try:
            certs = pkcs7.load_pem_pkcs7_certificates(p7b_bytes)
        except Exception as e2:
            print(f"Failed to parse P7B as PEM too: {e2}")
            return
            
    print(f"Found {len(certs)} certificates. Validating against {VSS_ENDPOINT} (policy: {VSS_POLICY})...")
    
    valid_certs = []
    invalid_certs = []
    error_certs = []
    
    for i, cert in enumerate(certs):
        print(f"Validating cert {i+1}/{len(certs)}...", end="\r")
        result = validate_cert(cert)
        
        subject = "Unknown"
        try:
            subject_attr = cert.subject.get_attributes_for_oid(x509.NameOID.COMMON_NAME)
            if subject_attr:
                subject = subject_attr[0].value
            else:
                subject = cert.subject.rfc4514_string()
        except:
            pass

        if "error" in result:
            error_certs.append((subject, result["error"]))
            continue
            
        validation_res = result.get("validationResult", {})
        status = validation_res.get("result", "UNKNOWN")
        
        if status == "SUCCESS":
            valid_certs.append(subject)
        else:
            reason = validation_res.get("invalidityReasonText", "Unknown reason")
            invalid_certs.append((subject, reason))
            
    print(" " * 50, end="\r") # clear progress line
    
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

if __name__ == "__main__":
    main()
