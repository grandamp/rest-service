#!/usr/bin/env python3
"""
Extract unique X.509 certificates observed in KeySupport VSS service logs
(local files, directories, stdin, or remote hosts over SSH) and file them
into locust/corpus/{valid,invalid,unclassified}/ as PEM files.

Usage:
  python3 locust/vss-log-mining/mine_vss_logs.py /path/to/vss/logs --vss https://keysupport.net --out locust/corpus
  python3 locust/vss-log-mining/mine_vss_logs.py --ssh user@host:/opt/vss/ext/logs --vss https://keysupport.net --out locust/corpus
  python3 locust/vss-log-mining/mine_vss_logs.py --ssh user@host:/opt/vss/ext/logs --use-log-result --out locust/corpus
"""

import argparse
import base64
import glob
import gzip
import hashlib
import json
import re
import shlex
import ssl
import subprocess
import sys
import urllib.request
from pathlib import Path
from typing import Iterable, Iterator, Optional
import warnings

from cryptography import x509
from cryptography.hazmat.primitives import serialization
from cryptography.utils import CryptographyDeprecationWarning

warnings.filterwarnings("ignore", category=CryptographyDeprecationWarning)

CERT_B64_RE = re.compile(r'"x509Certificate"\s*:\s*"([A-Za-z0-9+/=_\-\s]+)"')


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


def is_ca(cert: x509.Certificate) -> bool:
    try:
        bc = cert.extensions.get_extension_for_class(x509.BasicConstraints).value
        return bc.ca
    except (x509.ExtensionNotFound, ValueError):
        return False


def decode_der_cert(b64_str: str) -> Optional[tuple[x509.Certificate, str]]:
    cleaned = "".join(b64_str.split())
    if len(cleaned) < 64:
        return None
    for decoder in (base64.b64decode, base64.urlsafe_b64decode):
        try:
            padded = cleaned + ("=" * ((4 - len(cleaned) % 4) % 4))
            der = decoder(padded)
            if not der or der[0] != 0x30:
                continue
            cert = x509.load_der_x509_certificate(der)
            fp = hashlib.sha256(cert.public_bytes(serialization.Encoding.DER)).hexdigest()[:16]
            return cert, fp
        except Exception:
            continue
    return None


def _collect_from_x509_list(obj: object, out: list[str]) -> None:
    if isinstance(obj, list):
        for item in obj:
            if isinstance(item, str):
                out.append(item)
            elif isinstance(item, dict):
                val = item.get("x509Certificate")
                if isinstance(val, str):
                    out.append(val)
    elif isinstance(obj, dict):
        val = obj.get("x509Certificate")
        if isinstance(val, list):
            for s in val:
                if isinstance(s, str):
                    out.append(s)
        elif isinstance(val, str):
            out.append(val)


def extract_b64_and_outcome(line: str) -> tuple[list[str], Optional[str], Optional[str]]:
    """
    Returns (list_of_b64_certs, request_id, observed_result).
    """
    certs: list[str] = []
    req_id: Optional[str] = None
    observed_result: Optional[str] = None

    has_cert = '"x509Certificate"' in line
    has_result = '"validationResult"' in line
    if not has_cert and not has_result:
        return certs, req_id, observed_result

    try:
        record = json.loads(line)
        msg = record.get("message", record) if isinstance(record, dict) else None
        if isinstance(msg, str) and msg.lstrip().startswith("{"):
            msg = json.loads(msg)
        if isinstance(msg, dict):
            req_id_val = msg.get("requestId")
            if isinstance(req_id_val, str):
                req_id = req_id_val

            direct = msg.get("x509Certificate")
            if isinstance(direct, str):
                certs.append(direct)

            _collect_from_x509_list(msg.get("x509CertificateList"), certs)
            _collect_from_x509_list(msg.get("x509CertificatePath"), certs)

            vr = msg.get("validationResult")
            if isinstance(vr, dict):
                res_val = vr.get("result")
                if res_val in ("SUCCESS", "FAIL"):
                    observed_result = res_val
                _collect_from_x509_list(vr.get("x509CertificatePath"), certs)
                rbc_list = vr.get("resultsByCertificateList")
                if isinstance(rbc_list, list):
                    for entry in rbc_list:
                        if not isinstance(entry, dict):
                            continue
                        rbc = entry.get("resultByCertificate", entry)
                        if not isinstance(rbc, dict):
                            continue
                        vsd = rbc.get("validationSuccessData")
                        if not isinstance(vsd, dict):
                            continue
                        wbl = vsd.get("wantBackResultList")
                        if isinstance(wbl, list):
                            for wb in wbl:
                                if isinstance(wb, dict) and isinstance(wb.get("certPath"), dict):
                                    _collect_from_x509_list(
                                        wb["certPath"].get("x509CertificateList"), certs
                                    )
            if certs or observed_result:
                return certs, req_id, observed_result
    except Exception:
        pass

    if has_cert:
        certs.extend(m.group(1) for m in CERT_B64_RE.finditer(line))
    return certs, req_id, observed_result


def iter_local_files(inputs: list[str], file_glob: str) -> list[Path]:
    files: list[Path] = []
    seen: set[Path] = set()
    for raw in inputs:
        p = Path(raw)
        if p.is_file():
            resolved = p.resolve()
            if resolved not in seen:
                seen.add(resolved)
                files.append(p)
        elif p.is_dir():
            for match in sorted(p.glob(file_glob)):
                if match.is_file():
                    resolved = match.resolve()
                    if resolved not in seen:
                        seen.add(resolved)
                        files.append(match)
        else:
            for m in sorted(glob.glob(raw)):
                mp = Path(m)
                if mp.is_file():
                    resolved = mp.resolve()
                    if resolved not in seen:
                        seen.add(resolved)
                        files.append(mp)
    return files


def iter_lines_from_local(paths: list[Path]) -> Iterator[str]:
    for path in paths:
        opener = gzip.open if path.suffix == ".gz" else open
        try:
            with opener(path, "rt", encoding="utf-8", errors="replace") as fh:
                for line in fh:
                    yield line
        except OSError as e:
            print(f"[WARN] Could not read {path}: {e}", file=sys.stderr)


def iter_lines_from_ssh(
    ssh_spec: str,
    file_glob: str,
    identity: Optional[str],
    include_results: bool,
) -> Iterator[str]:
    if ":" not in ssh_spec:
        raise ValueError("--ssh must be in the form user@host:/remote/log/dir (or host:/path)")
    host_part, remote_dir = ssh_spec.split(":", 1)

    pattern = '"x509Certificate"|"validationResult"' if include_results else '"x509Certificate"'
    grep_flag = "-Eh" if include_results else "-Fh"
    quoted_dir = shlex.quote(remote_dir.rstrip("/"))
    # Filter on the remote host so only matching log lines cross the SSH channel.
    remote_cmd = (
        f"sh -c 'cd {quoted_dir} && "
        f"for f in {file_glob}; do "
        f"[ -f \"$f\" ] || continue; "
        f"case \"$f\" in "
        f"*.gz) gzip -dc \"$f\" | grep {grep_flag} {shlex.quote(pattern)} || true ;; "
        f"*) grep {grep_flag} {shlex.quote(pattern)} \"$f\" || true ;; "
        f"esac; "
        f"done'"
    )

    cmd = ["ssh", "-o", "BatchMode=yes"]
    if identity:
        cmd.extend(["-i", identity])
    cmd.extend([host_part, remote_cmd])

    proc = subprocess.Popen(
        cmd,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        encoding="utf-8",
        errors="replace",
        bufsize=1,
    )
    assert proc.stdout is not None
    assert proc.stderr is not None
    try:
        for line in proc.stdout:
            yield line
    finally:
        proc.stdout.close()
        stderr_out = proc.stderr.read().strip()
        proc.stderr.close()
        rc = proc.wait()
        if rc != 0:
            raise RuntimeError(f"SSH log stream failed (exit {rc}): {stderr_out}")


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


def process_stream(
    lines: Iterable[str],
    out_dir: Path,
    vss_base: str,
    policy_id: str,
    include_ca: bool,
    use_log_result: bool,
    reclassify: bool,
    ssl_context: ssl.SSLContext,
    timeout: int,
) -> dict[str, int]:
    (out_dir / "valid").mkdir(parents=True, exist_ok=True)
    (out_dir / "invalid").mkdir(parents=True, exist_ok=True)
    (out_dir / "unclassified").mkdir(parents=True, exist_ok=True)

    existing_on_disk = load_existing_corpus(out_dir)
    seen_b64: set[str] = set()
    seen_fps: set[str] = set()
    unique_certs: dict[str, tuple[x509.Certificate, Optional[str]]] = {}
    outcome_by_req: dict[str, str] = {}
    req_by_fp: dict[str, str] = {}

    stats = {
        "lines_scanned": 0,
        "certs_seen": 0,
        "unique_certs": 0,
        "skipped_ca": 0,
        "skipped_existing": 0,
        "valid": 0,
        "invalid": 0,
        "unclassified": 0,
    }

    for line in lines:
        stats["lines_scanned"] += 1
        b64_list, req_id, observed_result = extract_b64_and_outcome(line)
        if observed_result and req_id:
            outcome_by_req[req_id] = observed_result

        for b64 in b64_list:
            stats["certs_seen"] += 1
            if b64 in seen_b64:
                continue
            seen_b64.add(b64)
            decoded = decode_der_cert(b64)
            if decoded is None:
                continue
            cert, fp = decoded
            if req_id:
                req_by_fp[fp] = req_id
            if fp in seen_fps:
                continue
            seen_fps.add(fp)
            unique_certs[fp] = (cert, req_id)

    stats["unique_certs"] = len(unique_certs)
    print(
        f"Scanned {stats['lines_scanned']} line(s); "
        f"found {stats['unique_certs']} unique certificate(s) "
        f"from {stats['certs_seen']} log reference(s).",
        flush=True,
    )

    for fp, (cert, req_id) in unique_certs.items():
        if not include_ca and is_ca(cert):
            stats["skipped_ca"] += 1
            continue

        if not reclassify and fp in existing_on_disk:
            stats["skipped_existing"] += 1
            continue

        subj = cert.subject.rfc4514_string()
        bucket = "unclassified"
        detail = ""

        if vss_base:
            try:
                result, reason = validate_vss(
                    vss_base, policy_id, cert, ssl_context=ssl_context, timeout=timeout
                )
                bucket = "valid" if result == "SUCCESS" else "invalid"
                detail = f" -> {result}" + (f" ({reason})" if reason else "")
            except Exception as e:
                detail = f" -> VSS_ERROR ({e})"
        elif use_log_result:
            lookup_req = req_by_fp.get(fp) or req_id
            obs = outcome_by_req.get(lookup_req) if lookup_req else None
            if obs == "SUCCESS":
                bucket = "valid"
                detail = " -> SUCCESS (from log)"
            elif obs == "FAIL":
                bucket = "invalid"
                detail = " -> FAIL (from log)"

        old_path = existing_on_disk.get(fp)
        dest = out_dir / bucket / f"{fp}.pem"
        if old_path and old_path != dest and old_path.exists():
            old_path.unlink()

        dest.write_bytes(cert.public_bytes(serialization.Encoding.PEM))
        stats[bucket] += 1
        print(f"  [{bucket}] {dest.name} | {subj}{detail}", flush=True)

    return stats


def main():
    ap = argparse.ArgumentParser(
        description="Extract observed X.509 certificates from VSS service logs into locust/corpus."
    )
    ap.add_argument(
        "inputs",
        nargs="*",
        help="Local log file(s), directory(ies), glob pattern(s), or '-' for stdin",
    )
    ap.add_argument(
        "--ssh",
        default=None,
        help="Remote log directory in user@host:/path/to/logs format (streams matching lines over SSH)",
    )
    ap.add_argument(
        "--ssh-identity",
        default=None,
        help="Optional SSH private key path passed as -i to ssh",
    )
    ap.add_argument(
        "--file-glob",
        default="vss*.log*",
        help="Filename glob inside log directories (default: vss*.log*)",
    )
    ap.add_argument("--out", default="locust/corpus", help="Output corpus directory (default: locust/corpus)")
    ap.add_argument("--vss", default="", help="Optional VSS base URL to classify valid/invalid")
    ap.add_argument("--policy", default="1.3.6.1.5.5.7.19.1", help="Validation policy OID")
    ap.add_argument(
        "--use-log-result",
        action="store_true",
        help="When --vss is omitted, classify using validationResult observed in the logs",
    )
    ap.add_argument("--include-ca", action="store_true", help="Also save CA certificates")
    ap.add_argument(
        "--reclassify",
        action="store_true",
        help="Revalidate and overwrite/move certificates already present in --out",
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

    if not args.inputs and not args.ssh:
        ap.error("Provide local log path(s)/'-' or pass --ssh user@host:/path/to/logs.")

    try:
        ssl_ctx = build_ssl_context(args.ca_bundle, args.insecure)
    except FileNotFoundError as e:
        ap.error(str(e))

    out_dir = Path(args.out)

    def combined_lines() -> Iterator[str]:
        if args.inputs == ["-"]:
            for line in sys.stdin:
                yield line
            return
        local_inputs = [i for i in args.inputs if i != "-"]
        if local_inputs:
            local_files = iter_local_files(local_inputs, args.file_glob)
            print(f"Reading {len(local_files)} local log file(s)...", flush=True)
            yield from iter_lines_from_local(local_files)
        if "-" in args.inputs:
            for line in sys.stdin:
                yield line
        if args.ssh:
            print(f"Streaming matching log lines via SSH from {args.ssh} ({args.file_glob})...", flush=True)
            yield from iter_lines_from_ssh(
                args.ssh,
                file_glob=args.file_glob,
                identity=args.ssh_identity,
                include_results=(args.use_log_result and not args.vss),
            )

    stats = process_stream(
        combined_lines(),
        out_dir=out_dir,
        vss_base=args.vss,
        policy_id=args.policy,
        include_ca=args.include_ca,
        use_log_result=args.use_log_result,
        reclassify=args.reclassify,
        ssl_context=ssl_ctx,
        timeout=args.timeout,
    )

    print(
        "\nDone: "
        f"unique={stats['unique_certs']}, "
        f"added(valid={stats['valid']}, invalid={stats['invalid']}, unclassified={stats['unclassified']}), "
        f"skipped_existing={stats['skipped_existing']}, "
        f"skipped_ca={stats['skipped_ca']}"
    )


if __name__ == "__main__":
    main()
