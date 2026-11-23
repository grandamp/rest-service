#!/usr/bin/env python3
"""
Extract unique X.509 certificates across the last N commits of a PKCS#7 (.p7b)
bundle in GitHub (by default GSA/idmanagement.gov's FPKI intermediate bundle)
and file them into locust/corpus/{valid,invalid,unclassified}/ as PEM files.

Usage:
  python3 locust/fpki-intermediate-mine/mine_fpki_intermediates.py --vss https://keysupport.net --out locust/corpus
  python3 locust/fpki-intermediate-mine/mine_fpki_intermediates.py --versions 50 --out locust/corpus
"""

import argparse
import base64
import hashlib
import json
import os
import ssl
import sys
import urllib.error
import urllib.parse
import urllib.request
import warnings
from pathlib import Path
from typing import Optional

from cryptography import x509
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.serialization import pkcs7
from cryptography.utils import CryptographyDeprecationWarning

warnings.filterwarnings("ignore", category=CryptographyDeprecationWarning)

DEFAULT_REPO = "GSA/idmanagement.gov"
DEFAULT_BRANCH = "staging"
DEFAULT_PATH = "_implement/tools/CACertificatesValidatingToFederalCommonPolicyG2.p7b"
DEFAULT_USER_AGENT = "rest-service-fpki-intermediate-mine/1.0"


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


def github_headers(token: Optional[str], accept: str = "application/vnd.github+json") -> dict[str, str]:
    headers = {
        "User-Agent": DEFAULT_USER_AGENT,
        "Accept": accept,
    }
    if token:
        headers["Authorization"] = f"Bearer {token}"
    return headers


def fetch_commit_history(
    repo: str,
    branch: str,
    file_path: str,
    versions: int,
    token: Optional[str],
    ssl_context: ssl.SSLContext,
    timeout: int,
) -> list[dict[str, str]]:
    commits: list[dict[str, str]] = []
    page = 1
    while len(commits) < versions:
        per_page = min(100, versions - len(commits))
        query = urllib.parse.urlencode(
            {
                "sha": branch,
                "path": file_path,
                "per_page": str(per_page),
                "page": str(page),
            }
        )
        url = f"https://api.github.com/repos/{repo}/commits?{query}"
        req = urllib.request.Request(url, headers=github_headers(token))
        with urllib.request.urlopen(req, timeout=timeout, context=ssl_context) as resp:
            batch = json.load(resp)
        if not batch:
            break
        for item in batch:
            sha = item.get("sha")
            date = ((item.get("commit") or {}).get("committer") or {}).get("date", "")
            if sha:
                commits.append({"sha": sha, "date": date})
                if len(commits) >= versions:
                    break
        if len(batch) < per_page:
            break
        page += 1
    return commits


def fetch_raw_bundle(
    repo: str,
    sha: str,
    file_path: str,
    token: Optional[str],
    ssl_context: ssl.SSLContext,
    timeout: int,
) -> bytes:
    quoted_path = urllib.parse.quote(file_path.lstrip("/"))
    url = f"https://raw.githubusercontent.com/{repo}/{sha}/{quoted_path}"
    req = urllib.request.Request(url, headers=github_headers(token, accept="application/octet-stream"))
    with urllib.request.urlopen(req, timeout=timeout, context=ssl_context) as resp:
        return resp.read()


def extract_certs_from_bundle(raw_bytes: bytes) -> list[x509.Certificate]:
    for loader in (
        pkcs7.load_der_pkcs7_certificates,
        pkcs7.load_pem_pkcs7_certificates,
        x509.load_pem_x509_certificates,
    ):
        try:
            certs = loader(raw_bytes)
            if certs:
                return certs
        except Exception:
            continue
    try:
        return [x509.load_der_x509_certificate(raw_bytes)]
    except Exception:
        return []


def is_ca(cert: x509.Certificate) -> bool:
    try:
        bc = cert.extensions.get_extension_for_class(x509.BasicConstraints).value
        return bc.ca
    except (x509.ExtensionNotFound, ValueError):
        return False


def validate_vss(
    vss_base: str,
    policy_id: str,
    cert: x509.Certificate,
    ssl_context: ssl.SSLContext,
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
    with urllib.request.urlopen(req, timeout=timeout, context=ssl_context) as resp:
        body = json.load(resp)
    vr = body.get("validationResult", {})
    return vr.get("result", "UNKNOWN"), vr.get("invalidityReasonText", "")


def load_existing_corpus(out_dir: Path) -> dict[str, Path]:
    existing: dict[str, Path] = {}
    for bucket in ("valid", "invalid", "unclassified"):
        bdir = out_dir / bucket
        if bdir.is_dir():
            for pem in bdir.glob("*.pem"):
                existing[pem.stem] = pem
    return existing


def main() -> None:
    ap = argparse.ArgumentParser(
        description="Extract unique X.509 certificates across the last N GitHub commits of an FPKI .p7b bundle."
    )
    ap.add_argument("--repo", default=DEFAULT_REPO, help=f"GitHub owner/repo (default: {DEFAULT_REPO})")
    ap.add_argument("--branch", default=DEFAULT_BRANCH, help=f"Git branch or ref (default: {DEFAULT_BRANCH})")
    ap.add_argument("--path", default=DEFAULT_PATH, help=f"Path to .p7b file in repo (default: {DEFAULT_PATH})")
    ap.add_argument("--versions", type=int, default=50, help="Number of recent commits to mine (default: 50)")
    ap.add_argument("--out", default="locust/corpus", help="Output corpus directory (default: locust/corpus)")
    ap.add_argument("--vss", default="", help="Optional VSS base URL to classify valid/invalid")
    ap.add_argument("--policy", default="1.3.6.1.5.5.7.19.1", help="Validation policy OID")
    ap.add_argument("--ca-only", action="store_true", help="Skip non-CA certificates (e.g., end-entity certs)")
    ap.add_argument(
        "--reclassify",
        action="store_true",
        help="Revalidate and overwrite/move certificates already present in --out",
    )
    ap.add_argument(
        "--token",
        default=os.environ.get("GITHUB_TOKEN") or os.environ.get("GH_TOKEN"),
        help="Optional GitHub API token (defaults to GITHUB_TOKEN / GH_TOKEN env var)",
    )
    ap.add_argument(
        "--ca-bundle",
        default=None,
        help="Optional PEM bundle or CA directory for TLS verification",
    )
    ap.add_argument(
        "-k",
        "--insecure",
        action="store_true",
        help="Disable TLS verification",
    )
    ap.add_argument("--timeout", type=int, default=30, help="HTTP timeout in seconds (default: 30)")
    args = ap.parse_args()

    if args.versions < 1:
        ap.error("--versions must be >= 1")

    try:
        ssl_ctx = build_ssl_context(args.ca_bundle, args.insecure)
    except FileNotFoundError as e:
        ap.error(str(e))

    out_dir = Path(args.out)
    (out_dir / "valid").mkdir(parents=True, exist_ok=True)
    (out_dir / "invalid").mkdir(parents=True, exist_ok=True)
    (out_dir / "unclassified").mkdir(parents=True, exist_ok=True)

    print(
        f"Fetching last {args.versions} commit(s) for {args.repo}@{args.branch}:{args.path}...",
        flush=True,
    )
    commits = fetch_commit_history(
        repo=args.repo,
        branch=args.branch,
        file_path=args.path,
        versions=args.versions,
        token=args.token,
        ssl_context=ssl_ctx,
        timeout=args.timeout,
    )
    if not commits:
        print("No commits found for target path.", file=sys.stderr)
        sys.exit(1)

    seen_raw: set[bytes] = set()
    unique_certs: dict[str, x509.Certificate] = {}
    failed_fetches = 0

    for idx, c in enumerate(commits, start=1):
        sha = c["sha"]
        short_sha = sha[:8]
        date = c.get("date", "")
        try:
            bundle_bytes = fetch_raw_bundle(
                repo=args.repo,
                sha=sha,
                file_path=args.path,
                token=args.token,
                ssl_context=ssl_ctx,
                timeout=args.timeout,
            )
        except (urllib.error.HTTPError, urllib.error.URLError, TimeoutError, OSError) as e:
            failed_fetches += 1
            print(f"[{idx}/{len(commits)}] {short_sha} ({date}) [WARN] fetch failed: {e}", file=sys.stderr)
            continue

        certs = extract_certs_from_bundle(bundle_bytes)
        new_in_commit = 0
        for cert in certs:
            der = cert.public_bytes(serialization.Encoding.DER)
            if der in seen_raw:
                continue
            seen_raw.add(der)
            fp = hashlib.sha256(der).hexdigest()[:16]
            if fp not in unique_certs:
                unique_certs[fp] = cert
                new_in_commit += 1

        print(
            f"[{idx}/{len(commits)}] {short_sha} ({date}) -> {len(certs)} cert(s), {new_in_commit} new unique",
            flush=True,
        )

    existing_on_disk = load_existing_corpus(out_dir)
    stats = {
        "commits": len(commits),
        "failed_fetches": failed_fetches,
        "unique_certs": len(unique_certs),
        "skipped_existing": 0,
        "skipped_non_ca": 0,
        "valid": 0,
        "invalid": 0,
        "unclassified": 0,
    }

    for fp, cert in unique_certs.items():
        if args.ca_only and not is_ca(cert):
            stats["skipped_non_ca"] += 1
            continue
        if not args.reclassify and fp in existing_on_disk:
            stats["skipped_existing"] += 1
            continue

        subj = cert.subject.rfc4514_string()
        bucket = "unclassified"
        detail = ""
        if args.vss:
            try:
                result, reason = validate_vss(
                    args.vss, args.policy, cert, ssl_context=ssl_ctx, timeout=args.timeout
                )
                bucket = "valid" if result == "SUCCESS" else "invalid"
                detail = f" -> {result}" + (f" ({reason})" if reason else "")
            except Exception as e:
                detail = f" -> VSS_ERROR ({e})"

        old_path = existing_on_disk.get(fp)
        dest = out_dir / bucket / f"{fp}.pem"
        if old_path and old_path != dest and old_path.exists():
            old_path.unlink()

        dest.write_bytes(cert.public_bytes(serialization.Encoding.PEM))
        stats[bucket] += 1
        print(f"  [{bucket}] {dest.name} | {subj}{detail}", flush=True)

    print(
        "\nDone: "
        f"commits={stats['commits']} (failed={stats['failed_fetches']}), "
        f"unique={stats['unique_certs']}, "
        f"added(valid={stats['valid']}, invalid={stats['invalid']}, unclassified={stats['unclassified']}), "
        f"skipped_existing={stats['skipped_existing']}, "
        f"skipped_non_ca={stats['skipped_non_ca']}"
    )


if __name__ == "__main__":
    main()
