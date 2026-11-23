#!/usr/bin/env python3
"""
Extract X.509 signer (and bundled CA) certificates from signed PDF files
(e.g., publicly available .gov PDFs signed with PIV credentials) and
optionally validate them against a VSS endpoint.

Usage:
  python3 locust/harvest_pdf_certs.py --out locust/corpus --vss https://keysupport.net file1.pdf https://example.gov/signed.pdf
"""

import argparse
import base64
import hashlib
import json
import re
import ssl
import sys
import urllib.request
from pathlib import Path
from typing import Optional

from cryptography import x509
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.serialization import pkcs7

CONTENTS_RE = re.compile(rb"/Contents\s*<([0-9A-Fa-f\s]+)>")


def build_ssl_context(ca_bundle: Optional[str], insecure: bool) -> ssl.SSLContext:
    ctx = ssl.create_default_context()
    if insecure:
        ctx.check_hostname = False
        ctx.verify_mode = ssl.CERT_NONE
        return ctx
    if ca_bundle:
        ca_path = Path(ca_bundle)
        if not ca_path.exists():
            raise FileNotFoundError(f"CA bundle path not found: {ca_bundle}")
        if ca_path.is_dir():
            ctx.load_verify_locations(capath=str(ca_path))
            for pattern in ("*.pem", "*.crt", "*.cer"):
                for cert_file in sorted(ca_path.glob(pattern)):
                    if cert_file.is_file():
                        ctx.load_verify_locations(cafile=str(cert_file))
        else:
            ctx.load_verify_locations(cafile=str(ca_path))
    return ctx


def extract_certs_from_pdf_bytes(pdf_bytes: bytes) -> list[x509.Certificate]:
    certs: list[x509.Certificate] = []
    seen_fps: set[str] = set()
    for match in CONTENTS_RE.finditer(pdf_bytes):
        hex_str = re.sub(rb"\s+", b"", match.group(1)).decode("ascii")
        if len(hex_str) < 64:
            continue
        if len(hex_str) % 2 == 1:
            hex_str = hex_str[:-1]
        raw = bytes.fromhex(hex_str).rstrip(b"\x00")
        # Try stripping trailing zeros in 1-byte steps if needed for ASN.1 DER
        for trim in (0, 1, 2):
            candidate = raw if trim == 0 else raw + b"\x00" * trim
            try:
                extracted = pkcs7.load_der_pkcs7_certificates(candidate)
                for cert in extracted:
                    fp = hashlib.sha256(cert.public_bytes(serialization.Encoding.DER)).hexdigest()
                    if fp not in seen_fps:
                        seen_fps.add(fp)
                        certs.append(cert)
                break
            except Exception:
                continue
    return certs


def is_ca(cert: x509.Certificate) -> bool:
    try:
        bc = cert.extensions.get_extension_for_class(x509.BasicConstraints).value
        return bc.ca
    except (x509.ExtensionNotFound, ValueError) as e:
        if isinstance(e, ValueError):
            print(
                f"  [WARN] Malformed extension while checking BasicConstraints: {e}",
                file=sys.stderr,
            )
        return False


def validate_vss(vss_base: str, policy_id: str, cert: x509.Certificate) -> tuple[str, str]:
    der = cert.public_bytes(serialization.Encoding.DER)
    b64 = base64.b64encode(der).decode("ascii")
    req = urllib.request.Request(
        f"{vss_base.rstrip('/')}/vss/v2/validate",
        data=json.dumps({"validationPolicyId": policy_id, "x509Certificate": b64}).encode("utf-8"),
        headers={"Content-Type": "application/json", "Accept": "application/json"},
        method="POST",
    )
    with urllib.request.urlopen(req, timeout=30) as resp:
        body = json.load(resp)
    vr = body.get("validationResult", {})
    return vr.get("result", "UNKNOWN"), vr.get("invalidityReasonText", "")


def main():
    ap = argparse.ArgumentParser(description="Harvest X.509 certificates from signed PDFs")
    ap.add_argument("inputs", nargs="+", help="Local PDF paths or http(s):// URLs")
    ap.add_argument("--out", default="locust/corpus", help="Output directory")
    ap.add_argument("--vss", default="", help="Optional VSS base URL to classify valid/invalid")
    ap.add_argument("--policy", default="1.3.6.1.5.5.7.19.1", help="Validation policy OID")
    ap.add_argument("--include-ca", action="store_true", help="Also save CA certificates")
    ap.add_argument(
        "--ca-bundle",
        default=None,
        help="Optional PEM bundle or directory of extra trusted CAs (e.g., DoD roots)",
    )
    ap.add_argument(
        "-k",
        "--insecure",
        action="store_true",
        help="Disable TLS verification when fetching PDFs from legacy hosts",
    )
    args = ap.parse_args()

    try:
        ssl_ctx = build_ssl_context(args.ca_bundle, args.insecure)
    except FileNotFoundError as e:
        ap.error(str(e))

    out_dir = Path(args.out)
    (out_dir / "valid").mkdir(parents=True, exist_ok=True)
    (out_dir / "invalid").mkdir(parents=True, exist_ok=True)
    (out_dir / "unclassified").mkdir(parents=True, exist_ok=True)

    for src in args.inputs:
        if src.startswith(("http://", "https://")):
            req = urllib.request.Request(src, headers={"User-Agent": "rest-service-harvest/1.0"})
            with urllib.request.urlopen(req, timeout=30, context=ssl_ctx) as r:
                pdf_bytes = r.read()
        else:
            pdf_bytes = Path(src).read_bytes()

        certs = extract_certs_from_pdf_bytes(pdf_bytes)
        print(f"{src}: extracted {len(certs)} unique certificate(s)")
        for cert in certs:
            if not args.include_ca and is_ca(cert):
                continue
            fp = hashlib.sha256(cert.public_bytes(serialization.Encoding.DER)).hexdigest()[:16]
            subj = cert.subject.rfc4514_string()
            bucket = "unclassified"
            detail = ""
            if args.vss:
                result, reason = validate_vss(args.vss, args.policy, cert)
                bucket = "valid" if result == "SUCCESS" else "invalid"
                detail = f" -> {result}" + (f" ({reason})" if reason else "")
            dest = out_dir / bucket / f"{fp}.pem"
            dest.write_bytes(cert.public_bytes(serialization.Encoding.PEM))
            print(f"  [{bucket}] {dest.name} | {subj}{detail}")


if __name__ == "__main__":
    main()
