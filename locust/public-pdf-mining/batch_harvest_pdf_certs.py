#!/usr/bin/env python3
"""
Extract X.509 signer (and bundled CA) certificates from signed PDF files
(e.g., publicly available .gov PDFs signed with PIV credentials) and
optionally validate them against a VSS endpoint.

Usage:
  python3 locust/public-pdf-mining/batch_harvest_pdf_certs.py --out locust/corpus --vss https://keysupport.net -f locust/urls1.txt
  cat locust/urls1.txt | python3 locust/public-pdf-mining/batch_harvest_pdf_certs.py --out locust/corpus --vss https://keysupport.net -f -
  python3 locust/public-pdf-mining/batch_harvest_pdf_certs.py --out locust/corpus file1.pdf https://example.gov
"""

import argparse
import base64
import hashlib
import http.client
import json
import re
import ssl
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path
from typing import Optional

from cryptography import x509
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.serialization import pkcs7

CONTENTS_RE = re.compile(rb"/Contents\s*<([0-9A-Fa-f\s]+)>")

DEFAULT_USER_AGENT = (
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
    "AppleWebKit/537.36 (KHTML, like Gecko) "
    "Chrome/131.0.0.0 Safari/537.36"
)


def build_headers(user_agent: str) -> dict[str, str]:
    return {
        "User-Agent": user_agent,
        "Accept": "application/pdf,application/octet-stream,*/*;q=0.8",
        "Accept-Language": "en-US,en;q=0.9",
        "Connection": "keep-alive",
    }


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


def load_bulk_sources(cli_inputs: list[str], urls_file: Optional[str]) -> list[str]:
    """Combines positional inputs and a bulk URL file (or stdin via '-'), preserving order and removing duplicates."""
    raw_items: list[str] = list(cli_inputs)

    if urls_file:
        if urls_file == "-":
            lines = sys.stdin.read().splitlines()
        else:
            file_path = Path(urls_file)
            if not file_path.is_file():
                raise FileNotFoundError(f"Bulk URL file not found: {urls_file}")
            lines = file_path.read_text(encoding="utf-8").splitlines()

        for line in lines:
            cleaned = line.strip()
            if cleaned and not cleaned.startswith("#"):
                raw_items.append(cleaned)

    seen: set[str] = set()
    unique_sources: list[str] = []
    for item in raw_items:
        if item not in seen:
            seen.add(item)
            unique_sources.append(item)

    return unique_sources


def fetch_pdf(
    url: str,
    headers: dict[str, str],
    ssl_context: ssl.SSLContext,
    timeout: int = 30,
    max_retries: int = 2,
) -> Optional[bytes]:
    req = urllib.request.Request(url, headers=headers)

    for attempt in range(max_retries + 1):
        try:
            with urllib.request.urlopen(req, timeout=timeout, context=ssl_context) as resp:
                status = getattr(resp, "status", 200)
                if status != 200:
                    print(f"[SKIP] HTTP {status} for {url}", file=sys.stderr)
                    return None

                data = resp.read()
                if b"%PDF-" not in data[:1024]:
                    ctype = resp.headers.get("Content-Type", "unknown")
                    print(
                        f"[SKIP] Non-PDF payload (Content-Type: {ctype}) for {url}",
                        file=sys.stderr,
                    )
                    return None

                return data

        except urllib.error.HTTPError as e:
            if e.code in (403, 404, 410):
                print(f"[SKIP] HTTP {e.code} ({e.reason}) for {url}", file=sys.stderr)
                return None
            if e.code in (429, 500, 502, 503, 504) and attempt < max_retries:
                backoff = 1.5 * (attempt + 1)
                print(
                    f"[RETRY] HTTP {e.code} for {url}, sleeping {backoff}s...",
                    file=sys.stderr,
                )
                time.sleep(backoff)
                continue
            print(f"[ERROR] HTTP {e.code} ({e.reason}) for {url}", file=sys.stderr)
            return None

        except (urllib.error.URLError, http.client.HTTPException, ssl.SSLError, TimeoutError, OSError) as e:
            if attempt < max_retries:
                backoff = 1.5 * (attempt + 1)
                print(
                    f"[RETRY] Network error ({e}) for {url}, sleeping {backoff}s...",
                    file=sys.stderr,
                )
                time.sleep(backoff)
                continue
            print(f"[ERROR] Failed to reach {url}: {e}", file=sys.stderr)
            return None

    return None


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


def validate_vss(
    vss_base: str,
    policy_id: str,
    cert: x509.Certificate,
    timeout: int = 30,
) -> tuple[str, str]:
    der = cert.public_bytes(serialization.Encoding.DER)
    b64 = base64.b64encode(der).decode("ascii")
    req = urllib.request.Request(
        f"{vss_base.rstrip('/')}/vss/v2/validate",
        data=json.dumps({"validationPolicyId": policy_id, "x509Certificate": b64}).encode("utf-8"),
        headers={"Content-Type": "application/json", "Accept": "application/json"},
        method="POST",
    )
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        body = json.load(resp)
    vr = body.get("validationResult", {})
    return vr.get("result", "UNKNOWN"), vr.get("invalidityReasonText", "")


def main():
    ap = argparse.ArgumentParser(description="Harvest X.509 certificates from signed PDFs")
    ap.add_argument("inputs", nargs="*", help="Local PDF paths or http(s):// URLs")
    ap.add_argument(
        "-f",
        "--urls-file",
        default=None,
        help="Path to a newline-delimited file of PDF URLs/paths (use '-' for stdin)",
    )
    ap.add_argument("--out", default="locust/corpus", help="Output directory")
    ap.add_argument("--vss", default="", help="Optional VSS base URL to classify valid/invalid")
    ap.add_argument("--policy", default="1.3.6.1.5.5.7.19.1", help="Validation policy OID")
    ap.add_argument("--include-ca", action="store_true", help="Also save CA certificates")
    ap.add_argument(
        "--timeout",
        type=int,
        default=30,
        help="HTTP request timeout in seconds (default: 30)",
    )
    ap.add_argument(
        "--retries",
        type=int,
        default=2,
        help="Max retries on HTTP 429/5xx or network timeouts (default: 2)",
    )
    ap.add_argument(
        "--user-agent",
        default=DEFAULT_USER_AGENT,
        help="Custom User-Agent header for HTTP requests",
    )
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

    try:
        sources = load_bulk_sources(args.inputs, args.urls_file)
    except FileNotFoundError as e:
        ap.error(str(e))

    if not sources:
        ap.error("Provide at least one input path/URL or pass -f/--urls-file.")

    out_dir = Path(args.out)
    (out_dir / "valid").mkdir(parents=True, exist_ok=True)
    (out_dir / "invalid").mkdir(parents=True, exist_ok=True)
    (out_dir / "unclassified").mkdir(parents=True, exist_ok=True)

    headers = build_headers(args.user_agent)
    total = len(sources)
    stats = {"fetched": 0, "failed": 0, "valid": 0, "invalid": 0, "unclassified": 0}

    for idx, src in enumerate(sources, start=1):
        print(f"[{idx}/{total}] Processing {src}")
        if src.startswith(("http://", "https://")):
            pdf_bytes = fetch_pdf(
                src,
                headers=headers,
                ssl_context=ssl_ctx,
                timeout=args.timeout,
                max_retries=args.retries,
            )
            if pdf_bytes is None:
                stats["failed"] += 1
                continue
        else:
            try:
                pdf_bytes = Path(src).read_bytes()
            except OSError as e:
                print(f"[ERROR] Could not read {src}: {e}", file=sys.stderr)
                stats["failed"] += 1
                continue

        stats["fetched"] += 1
        certs = extract_certs_from_pdf_bytes(pdf_bytes)
        print(f"  Extracted {len(certs)} unique certificate(s)")

        for cert in certs:
            try:
                if not args.include_ca and is_ca(cert):
                    continue
                fp = hashlib.sha256(cert.public_bytes(serialization.Encoding.DER)).hexdigest()[:16]
                subj = cert.subject.rfc4514_string()
                bucket = "unclassified"
                detail = ""
                if args.vss:
                    try:
                        result, reason = validate_vss(
                            args.vss, args.policy, cert, timeout=args.timeout
                        )
                        bucket = "valid" if result == "SUCCESS" else "invalid"
                        detail = f" -> {result}" + (f" ({reason})" if reason else "")
                    except Exception as e:
                        detail = f" -> VSS_ERROR ({e})"
                stats[bucket] += 1
                dest = out_dir / bucket / f"{fp}.pem"
                dest.write_bytes(cert.public_bytes(serialization.Encoding.PEM))
                print(f"  [{bucket}] {dest.name} | {subj}{detail}", flush=True)
            except Exception as e:
                print(f"  [WARN] Skipping unparseable certificate: {e}", file=sys.stderr)

    print(
        "\nDone: "
        f"fetched={stats['fetched']}/{total}, "
        f"failed={stats['failed']}, "
        f"valid={stats['valid']}, "
        f"invalid={stats['invalid']}, "
        f"unclassified={stats['unclassified']}"
    )


if __name__ == "__main__":
    main()

