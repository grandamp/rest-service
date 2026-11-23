#!/usr/bin/env python3
"""
Verify and clean all certificates under locust/corpus/ against a live VSS
instance at the current point in time.

Re-validates every PEM file across valid/, invalid/, and unclassified/:
  - Moves newly expired or revoked certificates from valid/ -> invalid/
  - Promotes newly valid certificates (e.g., renewed trust/intermediate chain)
    from invalid/ or unclassified/ -> valid/
  - Canonicalizes filenames to <sha256[:16]>.pem and removes duplicate or
    unparseable certificate files

Usage:
  python3 locust/revalidate_corpus.py --vss https://keysupport.net --corpus locust/corpus
  python3 locust/revalidate_corpus.py --vss https://keysupport.net --dry-run
"""

import argparse
import base64
import concurrent.futures
import hashlib
import json
import ssl
import sys
import urllib.request
import warnings
from pathlib import Path
from typing import Optional

from cryptography import x509
from cryptography.hazmat.primitives import serialization
from cryptography.utils import CryptographyDeprecationWarning

warnings.filterwarnings("ignore", category=CryptographyDeprecationWarning)

BUCKETS = ("valid", "invalid", "unclassified")


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


def load_cert_file(path: Path) -> Optional[x509.Certificate]:
    raw = path.read_bytes()
    for loader in (x509.load_pem_x509_certificate, x509.load_der_x509_certificate):
        try:
            return loader(raw)
        except Exception:
            continue
    return None


def validate_vss(
    vss_base: str,
    policy_id: str,
    der_bytes: bytes,
    ssl_context: ssl.SSLContext,
    timeout: int = 30,
) -> tuple[str, str]:
    b64 = base64.b64encode(der_bytes).decode("ascii")
    req = urllib.request.Request(
        f"{vss_base.rstrip('/')}/vss/v2/validate",
        data=json.dumps({"validationPolicyId": policy_id, "x509Certificate": b64}).encode("utf-8"),
        headers={"Content-Type": "application/json", "Accept": "application/json"},
        method="POST",
    )
    with urllib.request.urlopen(req, timeout=timeout, context=ssl_context) as resp:
        body = json.load(resp)
    vr = body.get("validationResult", {})
    return vr.get("result", "UNKNOWN"), vr.get("invalidityReasonText", "")


def main() -> None:
    ap = argparse.ArgumentParser(
        description="Revalidate, deduplicate, and clean locust/corpus/ against a live VSS instance."
    )
    ap.add_argument(
        "--vss",
        default="https://keysupport.net",
        help="VSS base URL (default: https://keysupport.net)",
    )
    ap.add_argument(
        "--corpus",
        default="locust/corpus",
        help="Corpus root directory containing valid/, invalid/, unclassified/ (default: locust/corpus)",
    )
    ap.add_argument(
        "--policy",
        default="1.3.6.1.5.5.7.19.1",
        help="Validation policy OID (default: 1.3.6.1.5.5.7.19.1)",
    )
    ap.add_argument(
        "--workers",
        type=int,
        default=8,
        help="Concurrent validation requests (default: 8)",
    )
    ap.add_argument(
        "--dry-run",
        action="store_true",
        help="Report planned moves/removals without modifying files",
    )
    ap.add_argument(
        "--check",
        action="store_true",
        help="Dry-run check that exits non-zero if any corpus drift or invalid files are found",
    )
    ap.add_argument(
        "--ca-bundle",
        default=None,
        help="Optional PEM bundle or CA directory for --vss TLS verification",
    )
    ap.add_argument(
        "-k",
        "--insecure",
        action="store_true",
        help="Disable TLS verification for --vss requests",
    )
    ap.add_argument("--timeout", type=int, default=30, help="HTTP timeout in seconds (default: 30)")
    args = ap.parse_args()

    if args.workers < 1:
        ap.error("--workers must be >= 1")

    dry_run = args.dry_run or args.check

    try:
        ssl_ctx = build_ssl_context(args.ca_bundle, args.insecure)
    except FileNotFoundError as e:
        ap.error(str(e))

    corpus_dir = Path(args.corpus)
    for bucket in BUCKETS:
        (corpus_dir / bucket).mkdir(parents=True, exist_ok=True)

    # 1. Discover, parse, and deduplicate local files deterministically
    candidates: list[tuple[str, Path, x509.Certificate, bytes, str]] = []
    seen_fps: set[str] = set()
    corrupt_files: list[Path] = []
    duplicate_files: list[tuple[Path, str]] = []

    for bucket in BUCKETS:
        bdir = corpus_dir / bucket
        for path in sorted(bdir.iterdir()):
            if not path.is_file():
                continue
            cert = load_cert_file(path)
            if cert is None:
                corrupt_files.append(path)
                continue
            der = cert.public_bytes(serialization.Encoding.DER)
            fp = hashlib.sha256(der).hexdigest()[:16]
            if fp in seen_fps:
                duplicate_files.append((path, fp))
                continue
            seen_fps.add(fp)
            candidates.append((bucket, path, cert, der, fp))

    for path in corrupt_files:
        print(f"  [REMOVE corrupt] {path}")
        if not dry_run:
            path.unlink()

    for path, fp in duplicate_files:
        print(f"  [REMOVE duplicate] {path} (matches {fp}.pem)")
        if not dry_run:
            path.unlink()

    # 2. Revalidate all unique certificates against VSS
    def _check_one(item: tuple[str, Path, x509.Certificate, bytes, str]):
        bucket, path, cert, der, fp = item
        try:
            result, reason = validate_vss(
                args.vss, args.policy, der, ssl_context=ssl_ctx, timeout=args.timeout
            )
            return bucket, path, cert, fp, result, reason, None
        except Exception as exc:
            return bucket, path, cert, fp, "ERROR", "", str(exc)

    stats = {
        "total": len(candidates),
        "unchanged": 0,
        "moved": 0,
        "renamed": 0,
        "removed_corrupt": len(corrupt_files),
        "removed_duplicate": len(duplicate_files),
        "errors": 0,
        "valid": 0,
        "invalid": 0,
        "unclassified": 0,
    }
    transitions: dict[str, int] = {}

    with concurrent.futures.ThreadPoolExecutor(max_workers=args.workers) as pool:
        for bucket, path, cert, fp, result, reason, err in pool.map(_check_one, candidates):
            subj = cert.subject.rfc4514_string()
            if err is not None:
                stats["errors"] += 1
                stats[bucket] += 1
                print(f"  [ERROR] {path} | {subj} -> {err}", file=sys.stderr)
                continue

            if result == "SUCCESS":
                target_bucket = "valid"
            elif result == "FAIL":
                target_bucket = "invalid"
            else:
                target_bucket = "unclassified"

            stats[target_bucket] += 1
            dest = corpus_dir / target_bucket / f"{fp}.pem"
            detail = f" -> {result}" + (f" ({reason})" if reason else "")

            if bucket != target_bucket:
                key = f"{bucket}->{target_bucket}"
                transitions[key] = transitions.get(key, 0) + 1
                stats["moved"] += 1
                print(f"  [MOVE {bucket} -> {target_bucket}] {dest.name} | {subj}{detail}")
                if not dry_run:
                    dest.write_bytes(cert.public_bytes(serialization.Encoding.PEM))
                    if path != dest and path.exists():
                        path.unlink()
            elif path != dest:
                stats["renamed"] += 1
                print(f"  [RENAME {bucket}] {path.name} -> {dest.name} | {subj}")
                if not dry_run:
                    dest.write_bytes(cert.public_bytes(serialization.Encoding.PEM))
                    if path.exists():
                        path.unlink()
            else:
                stats["unchanged"] += 1

    mode_label = "DRY-RUN " if dry_run else ""
    trans_summary = ", ".join(f"{k}={v}" for k, v in sorted(transitions.items())) or "none"
    print(
        f"\n{mode_label}Done: checked={stats['total']}, unchanged={stats['unchanged']}, "
        f"moved={stats['moved']} ({trans_summary}), renamed={stats['renamed']}, "
        f"removed_corrupt={stats['removed_corrupt']}, removed_duplicate={stats['removed_duplicate']}, "
        f"errors={stats['errors']} | final(valid={stats['valid']}, invalid={stats['invalid']}, unclassified={stats['unclassified']})"
    )

    if stats["errors"] > 0:
        sys.exit(1)
    if args.check and (stats["moved"] > 0 or stats["renamed"] > 0 or stats["removed_corrupt"] > 0 or stats["removed_duplicate"] > 0):
        sys.exit(1)


if __name__ == "__main__":
    main()
