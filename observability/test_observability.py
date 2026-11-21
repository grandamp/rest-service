import json
import threading
import unittest
import urllib.request
from http.server import BaseHTTPRequestHandler, HTTPServer, ThreadingHTTPServer

import server as obs_server


class MockVssHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        routes = {
            "/actuator/health/liveness": {"status": "UP"},
            "/actuator/health/readiness": {"status": "UP"},
            "/actuator/metrics/vss.policies.loaded": {"measurements": [{"value": 3.0}]},
            "/actuator/metrics/vss.intermediates.cached": {"measurements": [{"value": 141.0}]},
            "/actuator/metrics/vss.validation.cache.entries": {"measurements": [{"value": 12.0}]},
            "/actuator/metrics/vss.validation.cache.hits": {"measurements": [{"value": 100.0}]},
            "/actuator/metrics/vss.validation.cache.misses": {"measurements": [{"value": 5.0}]},
            "/actuator/vssgraph": {
                "nodeCount": 2,
                "edgeCount": 1,
                "nodes": {"root": {"label": "COMMON"}, "ca1": {"label": "Test CA"}},
                "edges": [{"source": "root", "target": "ca1"}],
                "policies": [],
                "excludedFromBundle": [],
            },
        }
        payload = routes.get(self.path)
        if payload is None:
            self.send_response(404)
            self.end_headers()
            return
        body = json.dumps(payload).encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, format, *args):
        pass


class ObservabilityServerTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.mock_vss = HTTPServer(("127.0.0.1", 0), MockVssHandler)
        cls.mock_thread = threading.Thread(target=cls.mock_vss.serve_forever, daemon=True)
        cls.mock_thread.start()

        obs_server.VSS_BASE_URL = f"http://127.0.0.1:{cls.mock_vss.server_port}"
        obs_server._GRAPH_CACHE["data"] = None
        obs_server._STATUS_CACHE["data"] = None

        cls.ui_server = ThreadingHTTPServer(("127.0.0.1", 0), obs_server.Handler)
        cls.ui_thread = threading.Thread(target=cls.ui_server.serve_forever, daemon=True)
        cls.ui_thread.start()
        cls.base = f"http://127.0.0.1:{cls.ui_server.server_port}"

    @classmethod
    def tearDownClass(cls):
        cls.ui_server.shutdown()
        cls.ui_server.server_close()
        cls.mock_vss.shutdown()
        cls.mock_vss.server_close()

    def test_healthz_and_html(self):
        with urllib.request.urlopen(f"{self.base}/healthz") as r:
            self.assertEqual(200, r.status)
            self.assertEqual({"status": "UP"}, json.loads(r.read().decode()))
        with urllib.request.urlopen(f"{self.base}/") as r:
            self.assertEqual(200, r.status)
            html_body = r.read().decode()
            self.assertIn("./api/status", html_body)
            self.assertIn("./api/graph", html_body)

    def test_status_and_graph_subpath_endpoints(self):
        with urllib.request.urlopen(f"{self.base}/observability/api/status") as r:
            status = json.loads(r.read().decode())
            self.assertEqual("UP", status["readiness"])
            self.assertEqual("UP", status["liveness"])
            self.assertEqual(141.0, status["metrics"]["vss.intermediates.cached"])

        with urllib.request.urlopen(f"{self.base}/observability/api/graph") as r:
            graph = json.loads(r.read().decode())
            self.assertEqual("actuator/vssgraph", graph["telemetrySource"])
            self.assertEqual(2, graph["nodeCount"])


if __name__ == "__main__":
    unittest.main()
