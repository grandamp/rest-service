#!/usr/bin/env python3
import base64
import contextlib
import hashlib
import http.server
import io
import json
import pathlib
import sys
import tempfile
import threading
import unittest

from cryptography import x509
from cryptography.hazmat.primitives.serialization import Encoding, pkcs7

PYTHON_DIR = pathlib.Path(__file__).resolve().parent
if str(PYTHON_DIR) not in sys.path:
    sys.path.insert(0, str(PYTHON_DIR))

import intermediates
import pemhintlist
import policies
import validate
import validate_fpki

CERT_FIXTURE = (
    PYTHON_DIR.parent.parent
    / "src"
    / "test"
    / "resources"
    / "certs"
    / "ca"
    / "dod-id-ca-65.pem"
)


class _MockState:
    mode = "success"  # success | fail | mismatch | error_text | bad_json
    p7b_der = b""
    cert_b64 = ""
    cert_x5t = ""


class _MockHandler(http.server.BaseHTTPRequestHandler):
    def log_message(self, format: str, *args) -> None:  # noqa: A002
        return

    def do_GET(self) -> None:
        if self.path == "/bundle.p7b":
            self.send_response(200)
            self.send_header("Content-Type", "application/pkcs7-mime")
            self.end_headers()
            self.wfile.write(_MockState.p7b_der)
            return

        if self.path == "/vss/v2/intermediates/1.3.6.1.5.5.7.19.1":
            body = json.dumps([{"x509Certificate": _MockState.cert_b64}]).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(body)
            return

        if self.path == "/vss/v2/policies/1.3.6.1.5.5.7.19.1":
            body = json.dumps(
                {
                    "validationPolicyId": "1.3.6.1.5.5.7.19.1",
                    "trustAnchors": [{"x509Certificate": _MockState.cert_b64}],
                }
            ).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(body)
            return

        self.send_response(404)
        self.end_headers()
        self.wfile.write(b"Not Found")

    def do_POST(self) -> None:
        if self.path != "/vss/v2/validate":
            self.send_response(404)
            self.end_headers()
            return

        length = int(self.headers.get("Content-Length", "0"))
        _ = self.rfile.read(length)

        if _MockState.mode == "error_text":
            self.send_response(400)
            self.send_header("Content-Type", "text/plain")
            self.end_headers()
            self.wfile.write(b"Bad Request")
            return

        if _MockState.mode == "bad_json":
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(b"not-json")
            return

        x5t = "WRONG_DIGEST" if _MockState.mode == "mismatch" else _MockState.cert_x5t
        if _MockState.mode == "fail":
            val_res = {
                "result": "FAIL",
                "isAffirmativelyInvalid": True,
                "invalidityReasonText": "Certificate Revoked",
            }
        else:
            val_res = {"result": "SUCCESS"}

        body = json.dumps(
            {
                "x5t#S256": x5t,
                "x509SubjectName": "CN=DOD ID CA-65,OU=PKI,OU=DoD,O=U.S. Government,C=US",
                "x509IssuerName": "CN=DoD Root CA 3,OU=PKI,OU=DoD,O=U.S. Government,C=US",
                "validationResult": val_res,
            }
        ).encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(body)


class PythonClientTests(unittest.TestCase):
    server: http.server.ThreadingHTTPServer
    thread: threading.Thread
    base_url: str
    cert: x509.Certificate

    @classmethod
    def setUpClass(cls) -> None:
        pem_bytes = CERT_FIXTURE.read_bytes()
        cls.cert = x509.load_pem_x509_certificate(pem_bytes)
        der_bytes = cls.cert.public_bytes(Encoding.DER)
        _MockState.cert_b64 = base64.b64encode(der_bytes).decode("ascii")
        _MockState.cert_x5t = (
            base64.urlsafe_b64encode(hashlib.sha256(der_bytes).digest())
            .decode("ascii")
            .rstrip("=")
        )
        _MockState.p7b_der = pkcs7.serialize_certificates([cls.cert], Encoding.DER)

        cls.server = http.server.ThreadingHTTPServer(("127.0.0.1", 0), _MockHandler)
        cls.base_url = f"http://127.0.0.1:{cls.server.server_address[1]}"
        cls.thread = threading.Thread(target=cls.server.serve_forever, daemon=True)
        cls.thread.start()

    @classmethod
    def tearDownClass(cls) -> None:
        cls.server.shutdown()
        cls.server.server_close()

    def test_load_certificates_by_content_across_formats_and_extensions(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            tmp_dir = pathlib.Path(tmp)
            # PEM saved with .cer extension should still load via content sniffing
            pem_as_cer = tmp_dir / "pem_cert.cer"
            pem_as_cer.write_bytes(self.cert.public_bytes(Encoding.PEM))
            self.assertEqual(len(validate.load_certificates(str(pem_as_cer))), 1)

            # DER saved with .pem extension should still load via content sniffing
            der_as_pem = tmp_dir / "der_cert.pem"
            der_as_pem.write_bytes(self.cert.public_bytes(Encoding.DER))
            self.assertEqual(len(validate.load_certificates(str(der_as_pem))), 1)

            # DER PKCS#7 and PEM PKCS#7 bundles
            der_p7b = tmp_dir / "bundle.p7b"
            der_p7b.write_bytes(_MockState.p7b_der)
            self.assertEqual(len(validate.load_certificates(str(der_p7b))), 1)

            pem_p7b = tmp_dir / "bundle.pem"
            pem_p7b.write_bytes(pkcs7.serialize_certificates([self.cert], Encoding.PEM))
            self.assertEqual(len(validate.load_certificates(str(pem_p7b))), 1)

    def test_validate_cli_exit_codes_and_digest_verification(self) -> None:
        _MockState.mode = "success"
        out, err = io.StringIO(), io.StringIO()
        with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
            code = validate.main([str(CERT_FIXTURE), self.base_url + "/", "1.3.6.1.5.5.7.19.1", "-q"])
        self.assertEqual(code, 0)
        self.assertIn("Validation Result: SUCCESS", out.getvalue())

        _MockState.mode = "fail"
        out, err = io.StringIO(), io.StringIO()
        with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
            code = validate.main([str(CERT_FIXTURE), self.base_url, "1.3.6.1.5.5.7.19.1", "-q"])
        self.assertEqual(code, 1)
        self.assertIn("Certificate Revoked", out.getvalue())

        for bad_mode in ("mismatch", "error_text", "bad_json"):
            _MockState.mode = bad_mode
            out, err = io.StringIO(), io.StringIO()
            with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
                code = validate.main([str(CERT_FIXTURE), self.base_url, "1.3.6.1.5.5.7.19.1", "-q"])
            self.assertEqual(code, 2, f"Expected exit 2 for mode={bad_mode}")

        out, err = io.StringIO(), io.StringIO()
        with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
            code = validate.main(["/nonexistent/cert.pem", self.base_url, "1.3.6.1.5.5.7.19.1"])
        self.assertEqual(code, 2)

    def test_validate_fpki_round_trip_and_errors(self) -> None:
        p7b_url = f"{self.base_url}/bundle.p7b"

        _MockState.mode = "success"
        out, err = io.StringIO(), io.StringIO()
        with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
            code = validate_fpki.main(["--host", self.base_url, "--p7b-url", p7b_url])
        self.assertEqual(code, 0)
        self.assertIn("VALID CERTIFICATES (1)", out.getvalue())

        _MockState.mode = "fail"
        out, err = io.StringIO(), io.StringIO()
        with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
            code = validate_fpki.main(["--host", self.base_url, "--p7b-url", p7b_url])
        self.assertEqual(code, 1)
        self.assertIn("INVALID CERTIFICATES (1)", out.getvalue())

        _MockState.mode = "mismatch"
        out, err = io.StringIO(), io.StringIO()
        with contextlib.redirect_stdout(out), contextlib.redirect_stderr(err):
            code = validate_fpki.main(["--host", self.base_url, "--p7b-url", p7b_url])
        self.assertEqual(code, 2)
        self.assertIn("VALIDATION ERRORS (1)", out.getvalue())

    def test_auxiliary_scripts(self) -> None:
        policy = "1.3.6.1.5.5.7.19.1"

        out = io.StringIO()
        with contextlib.redirect_stdout(out):
            self.assertEqual(intermediates.main([self.base_url + "/", policy]), 0)
        self.assertIn("-----BEGIN CERTIFICATE-----", out.getvalue())

        out = io.StringIO()
        with contextlib.redirect_stdout(out):
            self.assertEqual(policies.main([self.base_url, policy]), 0)
        self.assertIn("trustAnchors", out.getvalue())

        out = io.StringIO()
        with contextlib.redirect_stdout(out):
            self.assertEqual(pemhintlist.main([self.base_url, policy]), 0)
        self.assertEqual(out.getvalue().count("-----BEGIN CERTIFICATE-----"), 2)

        err = io.StringIO()
        with contextlib.redirect_stderr(err):
            self.assertEqual(policies.main([self.base_url, "missing-policy"]), 2)
        self.assertIn("Error fetching policy", err.getvalue())


if __name__ == "__main__":
    unittest.main()
