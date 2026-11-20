#!/usr/bin/env python3
"""
Locust load test for the KeySupport Validation Service (VSS).

Loads test-harness certificates from src/test/resources/certs/ (and any
optional PEM files under locust/corpus/valid/ or locust/corpus/invalid/)
and exercises /vss/v2/validate, /vss/v2/policies, and /vss/v2/intermediates.
"""

import glob
import os
import resource
from pathlib import Path
from time import gmtime, strftime

from locust import FastHttpUser, between, task

# Raise open-file soft limit safely without exceeding the OS hard limit
try:
    soft, hard = resource.getrlimit(resource.RLIMIT_NOFILE)
    target = min(65535, hard) if hard != resource.RLIM_INFINITY else 65535
    if soft < target:
        resource.setrlimit(resource.RLIMIT_NOFILE, (target, hard))
except (ValueError, OSError):
    pass

REPO_ROOT = Path(__file__).resolve().parent.parent
CERTS_DIR = REPO_ROOT / "src" / "test" / "resources" / "certs"
CORPUS_DIR = Path(__file__).resolve().parent / "corpus"
DEFAULT_POLICY = os.environ.get("VSS_POLICY_ID", "1.3.6.1.5.5.7.19.1")


def _load_pem_b64(path: Path) -> str:
    lines = [
        line.strip()
        for line in path.read_text(encoding="utf-8").splitlines()
        if line.strip() and not line.startswith("-----")
    ]
    return "".join(lines)


def _discover_certs():
    valid_ca = [
        (p.name, _load_pem_b64(p))
        for p in sorted((CERTS_DIR / "ca").glob("*.pem"))
    ]

    valid_ee_names = {"xtec-pivi-dea-uuid.pem"}
    valid_ee = []
    negative_ee = []
    for p in sorted((CERTS_DIR / "ee").glob("*.pem")):
        entry = (p.name, _load_pem_b64(p))
        if p.name in valid_ee_names:
            valid_ee.append(entry)
        else:
            negative_ee.append(entry)

    # Optional harvested telemetry corpus (gitignored)
    for p in sorted(glob.glob(str(CORPUS_DIR / "valid" / "*.pem"))):
        path = Path(p)
        valid_ee.append((path.name, _load_pem_b64(path)))
    for p in sorted(glob.glob(str(CORPUS_DIR / "invalid" / "*.pem"))):
        path = Path(p)
        negative_ee.append((path.name, _load_pem_b64(path)))

    return valid_ca, valid_ee, negative_ee


VALID_CA_CERTS, VALID_EE_CERTS, NEGATIVE_EE_CERTS = _discover_certs()


class VssUser(FastHttpUser):
    insecure = True
    wait_time = between(
        float(os.environ.get("VSS_WAIT_MIN", "0.1")),
        float(os.environ.get("VSS_WAIT_MAX", "0.5")),
    )

    def _validate(self, name: str, b64_cert: str, expected_result: str, idx: int):
        ts = strftime("%Y-%m-%dT%H:%M:%SZ", gmtime())
        payload = {
            "validationPolicyId": DEFAULT_POLICY,
            "x509Certificate": b64_cert,
            "clientMetadata": {"client": f"Locust {name} {ts}"},
        }
        with self.client.post(
            "/vss/v2/validate",
            json=payload,
            name=f"POST /vss/v2/validate ({expected_result})",
            catch_response=True,
        ) as response:
            if response.status_code != 200:
                response.failure(f"{name}: HTTP {response.status_code}")
                return
            try:
                body = response.json()
                actual = body.get("validationResult", {}).get("result")
            except Exception as exc:
                response.failure(f"{name}: invalid JSON ({exc})")
                return
            if actual != expected_result:
                reason = body.get("validationResult", {}).get("invalidityReasonText", "")
                response.failure(f"{name}: expected {expected_result}, got {actual} ({reason})")
            else:
                response.success()

    def on_start(self):
        self._ca_idx = 0
        self._ee_idx = 0
        self._neg_idx = 0

    @task(5)
    def validate_valid_ee(self):
        if not VALID_EE_CERTS:
            return
        name, b64 = VALID_EE_CERTS[self._ee_idx % len(VALID_EE_CERTS)]
        self._ee_idx += 1
        self._validate(name, b64, "SUCCESS", self._ee_idx)

    @task(3)
    def validate_valid_ca(self):
        if not VALID_CA_CERTS:
            return
        name, b64 = VALID_CA_CERTS[self._ca_idx % len(VALID_CA_CERTS)]
        self._ca_idx += 1
        self._validate(name, b64, "SUCCESS", self._ca_idx)

    @task(2)
    def validate_negative_ee(self):
        if not NEGATIVE_EE_CERTS:
            return
        name, b64 = NEGATIVE_EE_CERTS[self._neg_idx % len(NEGATIVE_EE_CERTS)]
        self._neg_idx += 1
        self._validate(name, b64, "FAIL", self._neg_idx)

    @task(1)
    def get_policies(self):
        with self.client.get("/vss/v2/policies", name="GET /vss/v2/policies", catch_response=True) as r:
            if r.status_code == 200:
                r.success()
            else:
                r.failure(f"HTTP {r.status_code}")

    @task(1)
    def get_intermediates(self):
        with self.client.get("/vss/v2/intermediates", name="GET /vss/v2/intermediates", catch_response=True) as r:
            if r.status_code == 200:
                r.success()
            else:
                r.failure(f"HTTP {r.status_code}")
