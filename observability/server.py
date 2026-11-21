#!/usr/bin/env python3
"""
Generic observability web UI and telemetry proxy for KeySupport rest-service.

Queries a configured rest-service Actuator & VSS API base URL over HTTP/HTTPS
and serves a radial per-policy FPKI Graph and live cache/readiness dashboard.
"""
import html
import json
import os
import ssl
import threading
import time
import urllib.error
import urllib.request
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import parse_qs, urlparse

PORT = int(os.environ.get("PORT", "8099"))
BIND_HOST = os.environ.get("BIND_HOST", "0.0.0.0")
VSS_BASE_URL = os.environ.get("VSS_BASE_URL", "http://127.0.0.1:8000").rstrip("/")
VSS_TLS_VERIFY = os.environ.get("VSS_TLS_VERIFY", "true").lower() not in ("0", "false", "no")
GRAPH_CACHE_SECONDS = int(os.environ.get("GRAPH_CACHE_SECONDS", "60"))
STATUS_CACHE_SECONDS = int(os.environ.get("STATUS_CACHE_SECONDS", "3"))
DASHBOARD_TITLE = os.environ.get("DASHBOARD_TITLE", "KeySupport VSS · Observability & FPKI Graph")

METRICS_NAMES = (
    "vss.policies.loaded",
    "vss.intermediates.cached",
    "vss.validation.cache.entries",
    "vss.validation.cache.hits",
    "vss.validation.cache.misses",
)

_CACHE_LOCK = threading.Lock()
_GRAPH_CACHE = {"ts": 0.0, "data": None}
_STATUS_CACHE = {"ts": 0.0, "data": None}


def _ssl_context():
    if VSS_TLS_VERIFY:
        return None
    ctx = ssl.create_default_context()
    ctx.check_hostname = False
    ctx.verify_mode = ssl.CERT_NONE
    return ctx


def fetch_json(base_url, path, timeout=8):
    url = f"{base_url.rstrip('/')}{path}"
    req = urllib.request.Request(url, headers={"Accept": "application/json"})
    with urllib.request.urlopen(req, timeout=timeout, context=_ssl_context()) as resp:
        return json.loads(resp.read().decode("utf-8"))


def collect_status(base_url=None, force=False):
    target = (base_url or VSS_BASE_URL).rstrip("/")
    now = time.time()
    if not force and base_url is None:
        with _CACHE_LOCK:
            if _STATUS_CACHE["data"] is not None and (now - _STATUS_CACHE["ts"]) < STATUS_CACHE_SECONDS:
                return _STATUS_CACHE["data"]

    try:
        liveness = fetch_json(target, "/actuator/health/liveness", timeout=4).get("status", "UNKNOWN")
    except Exception:
        liveness = "UNKNOWN"

    try:
        readiness = fetch_json(target, "/actuator/health/readiness", timeout=4).get("status", "UNKNOWN")
    except Exception:
        readiness = "UNKNOWN"

    metrics = {}
    errors = []
    for name in METRICS_NAMES:
        try:
            payload = fetch_json(target, f"/actuator/metrics/{name}", timeout=4)
            measurements = payload.get("measurements") or []
            metrics[name] = measurements[0]["value"] if measurements else 0.0
        except Exception as exc:
            errors.append(f"{name}: {exc}")

    data = {
        "liveness": liveness,
        "readiness": readiness,
        "metrics": metrics,
        "collectedAt": int(now),
    }
    if errors and not metrics:
        data["error"] = "; ".join(errors)

    if base_url is None:
        with _CACHE_LOCK:
            _STATUS_CACHE["data"] = data
            _STATUS_CACHE["ts"] = now
    return data


def collect_graph(base_url=None, force=False):
    target = (base_url or VSS_BASE_URL).rstrip("/")
    now = time.time()
    if not force and base_url is None:
        with _CACHE_LOCK:
            if _GRAPH_CACHE["data"] is not None and (now - _GRAPH_CACHE["ts"]) < GRAPH_CACHE_SECONDS:
                return _GRAPH_CACHE["data"]

    try:
        data = fetch_json(target, "/actuator/vssgraph", timeout=15)
        data["telemetrySource"] = "actuator/vssgraph"
        data["collectedAt"] = int(now)
    except urllib.error.HTTPError as exc:
        if exc.code == 404:
            return {"error": "/actuator/vssgraph is not enabled on the upstream VSS instance (HTTP 404)"}
        return {"error": f"Upstream /actuator/vssgraph returned HTTP {exc.code}"}
    except Exception as exc:
        return {"error": f"Failed to fetch /actuator/vssgraph: {exc}"}

    if base_url is None:
        with _CACHE_LOCK:
            _GRAPH_CACHE["data"] = data
            _GRAPH_CACHE["ts"] = now
    return data


HTML_TEMPLATE = r"""<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>__TITLE__</title>
  <style>
    :root {
      --bg: #f1f6f7;
      --card: #ffffff;
      --surface: #f8fbfa;
      --ink: #153d51;
      --muted: #526e7c;
      --line: #d9e5e9;
      --accent: #215d77;
      --ok: #0d7a5f;
      --ok-bg: #e6f6f0;
      --bad: #b42318;
      --bad-bg: #fef2f2;
      --gold-fill: #fff8e8;
      --gold-stroke: #b4975e;
      --selected-fill: #f8d78e;
      --selected-stroke: #a4771b;
    }
    * { box-sizing: border-box; }
    body {
      margin: 0;
      font-family: 'Source Sans 3', 'Source Sans', 'Segoe UI', Arial, sans-serif;
      background: var(--bg);
      color: var(--ink);
    }
    button, input { font: inherit; }
    button { cursor: pointer; }
    button:disabled { cursor: default; opacity: .4; }
    button:focus-visible, input:focus-visible { outline: 3px solid #ffbd32; outline-offset: 2px; }
    [hidden] { display: none !important; }

    .topbar {
      background: #0f2c3c;
      color: #fff;
      padding: 12px 20px;
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
    }
    .topbar h1 { margin: 0; font-size: 1.15rem; font-weight: 700; letter-spacing: .01em; }
    .topbar .sub { font-size: .8rem; color: #b7d2de; margin-top: 2px; }
    .topbar-actions { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
    .btn-top {
      background: rgba(255,255,255,.12);
      color: #fff;
      border: 1px solid rgba(255,255,255,.25);
      border-radius: 6px;
      padding: 6px 11px;
      font-size: .82rem;
      font-weight: 600;
    }
    .btn-top:hover { background: rgba(255,255,255,.22); }
    .btn-top.active { background: #fff; color: #0f2c3c; border-color: #fff; }

    .wrap { max-width: 1360px; margin: 0 auto; padding: 14px 18px 28px; }

    .kpi-strip {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: 10px;
      margin-bottom: 14px;
    }
    .kpi {
      background: var(--card);
      border: 1px solid var(--line);
      border-radius: 8px;
      padding: 10px 12px;
    }
    .kpi-label { font-size: .72rem; text-transform: uppercase; letter-spacing: .04em; color: var(--muted); font-weight: 700; }
    .kpi-val { font-size: 1.25rem; font-weight: 700; margin-top: 4px; }
    .badge { display: inline-block; padding: 2px 9px; border-radius: 999px; font-size: .78rem; font-weight: 700; }
    .up { background: var(--ok-bg); color: var(--ok); border: 1px solid #a7dfc9; }
    .down { background: var(--bad-bg); color: var(--bad); border: 1px solid #f7b8b3; }

    .policy-bar {
      background: var(--card);
      border: 1px solid var(--line);
      border-radius: 8px;
      padding: 12px 14px;
      margin-bottom: 12px;
    }
    .policy-pills { display: flex; flex-wrap: wrap; gap: 8px; align-items: center; }
    .policy-pill {
      border: 1px solid #b4cad3;
      background: #f8fafb;
      color: var(--ink);
      border-radius: 7px;
      padding: 7px 12px;
      text-align: left;
      font-size: .86rem;
    }
    .policy-pill:hover { background: #e9f1f4; }
    .policy-pill.selected { background: #153d51; color: #fff; border-color: #153d51; }
    .policy-pill .pid { display: block; font-size: .72rem; opacity: .82; font-family: ui-monospace, monospace; }
    .policy-meta {
      margin-top: 9px; padding-top: 9px; border-top: 1px solid var(--line);
      font-size: .82rem; color: var(--muted); display: flex; flex-wrap: wrap; gap: 10px 20px; align-items: center;
    }
    .policy-meta code { background: #eef4f6; color: var(--ink); padding: 1px 5px; border-radius: 4px; font-size: .78rem; }

    .fpki-shell {
      background: var(--card);
      border: 1px solid #b4cad3;
      border-radius: 8px;
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }
    .fpki-shell.expanded {
      position: fixed; inset: 12px; z-index: 1000;
      box-shadow: 0 24px 64px rgba(10,30,45,.35);
    }
    .fpki-bar {
      display: flex; align-items: center; justify-content: space-between;
      padding: 9px 14px; background: #f1f6f7; border-bottom: 1px solid var(--line); font-size: .92rem;
    }
    .fpki-bar button {
      border: 1px solid var(--line); background: #fff; color: var(--ink);
      border-radius: 5px; padding: 5px 10px; font-size: .8rem; font-weight: 600;
    }
    .fpki-bar button:hover { background: #e9f1f4; }

    .widget {
      height: 740px;
      display: flex;
      flex-direction: column;
      gap: 9px;
      padding: 10px 14px 0;
      background: #fff;
    }
    .fpki-shell.expanded .widget { height: calc(100% - 44px); }

    .toolbar { display: flex; gap: 8px; flex-shrink: 0; }
    .toolbar form {
      display: flex; flex: 1; min-width: 0; align-items: center;
      border: 1px solid #b4cad3; border-radius: 5px; overflow: hidden; background: #fff;
    }
    .search-symbol { font-size: 22px; padding: 0 9px; color: #67818e; line-height: 1; }
    .toolbar input { border: 0; background: transparent; width: 100%; padding: 9px 0; font-size: 14px; color: var(--ink); outline: none; }
    .toolbar form button {
      border: 0; border-left: 1px solid var(--line); border-radius: 0;
      padding: 9px 14px; background: #eef4f6; color: var(--ink); font-size: 14px; font-weight: 600;
    }
    .toolbar > button {
      border: 1px solid var(--line); border-radius: 5px; background: #f8fafb;
      color: var(--ink); padding: 7px 14px; font-size: 14px;
    }
    .toolbar button:hover:not(:disabled) { background: #e9f1f4; }

    .navigation { display: flex; align-items: center; justify-content: space-between; gap: 8px; flex-shrink: 0; min-height: 30px; }
    .nav-buttons { display: flex; gap: 6px; flex-wrap: wrap; }
    .navigation button {
      font-size: 12px; border: 1px solid transparent; border-radius: 5px;
      background: #f0f5f6; color: var(--ink); padding: 6px 10px;
    }
    .navigation button:hover:not(:disabled) { background: #e2ecef; }
    .direction { font-size: 11px; color: var(--muted); white-space: nowrap; }
    .direction span { font-size: 18px; vertical-align: middle; margin: 0 4px; }

    .surface, .search-results, .details-panel, .excluded-panel {
      flex: 1; min-height: 0; overflow: auto;
      border: 1px solid var(--line); border-radius: 7px;
      background: var(--surface); position: relative;
    }
    .surface { display: flex; flex-direction: column; }
    .search-results, .details-panel, .excluded-panel { background: #fff; padding: 14px 16px; }
    .panel-head {
      display: flex; justify-content: space-between; align-items: center; gap: 10px;
      position: sticky; top: -14px; background: #fff; padding: 8px 0 12px; z-index: 3;
      border-bottom: 1px solid var(--line); margin-bottom: 10px;
    }
    .panel-head h2 { margin: 0; font-size: 18px; }
    .panel-head button { border: 1px solid var(--line); background: #fff; border-radius: 5px; padding: 6px 10px; font-size: 13px; color: var(--ink); }

    .search-results ul { list-style: none; margin: 0; padding: 0; }
    .search-results li { border-bottom: 1px solid var(--line); }
    .search-results li button {
      display: block; text-align: left; width: 100%; border: 0; background: transparent;
      padding: 11px 8px; font-size: 14px; color: var(--ink);
    }
    .search-results li button:hover { background: #f1f6f7; }
    .search-results li button span { display: block; color: var(--muted); font-size: 11px; margin-top: 3px; }

    .context-path { display: flex; align-items: center; flex-wrap: wrap; gap: 4px; font-size: 11px; padding: 10px 16px 0; color: var(--muted); }
    .context-path button {
      font-size: 11px; border: 0; padding: 2px 3px; background: transparent;
      color: var(--accent); text-decoration: underline; text-underline-offset: 2px;
    }
    .context-path > span:last-child { font-weight: 700; color: var(--ink); }
    .surface-title { padding: 8px 16px 4px; font-size: 19px; line-height: 1.25; font-weight: 700; margin: 0; }
    .surface-caption { padding: 0 16px 8px; font-size: 11px; color: var(--muted); margin: 0; }
    .authority-about { padding: 0 16px 8px; font-size: 13px; line-height: 1.45; color: var(--muted); }
    .authority-about p { margin: 2px 0; }
    .authority-affiliation { font-weight: 700; color: #365969; }
    .issuer-node {
      border: 1px solid var(--line); border-radius: 5px; background: #fff;
      color: var(--ink); padding: 2px 8px; font-size: 12px; margin: 0 4px 2px 0;
    }
    .issuer-node:hover { background: #e9f1f4; }
    .issuer-dot { color: var(--accent); margin-right: 4px; font-weight: 700; }

    .radial-wrap { position: relative; flex-shrink: 0; width: 100%; }
    .radial-svg { display: block; width: 100%; overflow: visible; }
    .radial-svg .ring { fill: none; stroke: #dce6e6; stroke-dasharray: 2 5; }
    .radial-edge { fill: none; stroke-width: 1.25; opacity: .78; }
    .point-hit { fill: transparent; stroke: none; cursor: pointer; }
    .point-circle { stroke-width: 1.3; cursor: pointer; }
    .root-mark { fill: var(--gold-fill); stroke: var(--gold-stroke); }
    .root-label { font-size: 9px; fill: #735c2b; font-weight: 700; pointer-events: none; }
    .point-number { font-size: 9px; font-weight: 700; pointer-events: none; }
    .selected-point { fill: var(--selected-fill) !important; stroke: var(--selected-stroke) !important; stroke-width: 2.5 !important; }

    .branch-label {
      position: absolute; display: grid; grid-template-columns: 23px minmax(0,1fr);
      align-items: center; gap: 7px; border: 0; background: transparent;
      text-align: left; padding: 4px; font-size: 12.5px; line-height: 1.25; color: #2a4b59;
      border-radius: 4px;
    }
    .branch-label .number {
      border: 1px solid color-mix(in srgb, var(--branch-color), white 55%);
      border-radius: 50%; width: 22px; height: 22px; display: grid; place-items: center;
      color: var(--branch-color); font-size: 10px; font-weight: 700; background: #fff;
    }
    .branch-label:hover { background: #e8f0ef; }
    .branch-list { display: grid; grid-template-columns: repeat(2, minmax(0,1fr)); gap: 4px; padding: 6px 14px 14px; }
    .branch-list .branch-label { position: static; width: auto; }

    .node-tooltip {
      position: fixed; z-index: 1200; pointer-events: none;
      background: #0f2c3c; color: #fff; padding: 6px 10px; border-radius: 6px;
      font-size: 12px; box-shadow: 0 6px 20px rgba(0,0,0,.22); max-width: 300px;
    }

    .details-panel dl { font-size: 13px; margin: 0; }
    .details-panel dt { font-weight: 700; margin-top: 12px; color: var(--ink); }
    .details-panel dd { margin: 4px 0; overflow-wrap: anywhere; color: #2a4b59; font-family: ui-monospace, monospace; font-size: 12px; }
    .details-panel h3 { font-size: 15px; margin: 18px 0 8px; border-top: 1px solid var(--line); padding-top: 12px; }
    .rel-list { display: flex; flex-wrap: wrap; gap: 6px; }
    .rel-list button { border: 1px solid var(--line); background: #f8fafb; border-radius: 5px; padding: 5px 10px; font-size: 12px; color: var(--ink); }
    .rel-list button:hover { background: #e9f1f4; }

    .oid-breakdown {
      margin: 4px 16px 10px;
      background: #fff;
      border: 1px solid var(--line);
      border-radius: 7px;
      padding: 10px 12px;
      font-size: 12px;
    }
    .oid-breakdown-head {
      display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 8px;
      margin-bottom: 6px; font-weight: 700; color: var(--ink);
    }
    .oid-chips { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 8px; }
    .oid-chip {
      display: inline-flex; align-items: center; gap: 5px;
      border-radius: 5px; padding: 3px 8px; font-size: 11px;
      border: 1px solid var(--line); background: #f8fafb; color: var(--muted);
    }
    .oid-chip.valid { background: var(--ok-bg); border-color: #a7dfc9; color: #0b5d48; }
    .oid-chip.mapped { background: #eef6ff; border-color: #bfdbfe; color: #1e40af; }
    .oid-chip code { font-family: ui-monospace, monospace; font-size: 10.5px; }
    .oid-matrix { width: 100%; border-collapse: collapse; font-size: 11.5px; }
    .oid-matrix th, .oid-matrix td { padding: 6px 8px; border-top: 1px solid var(--line); vertical-align: top; text-align: left; }
    .oid-matrix th { color: var(--muted); font-size: 10.5px; text-transform: uppercase; letter-spacing: .03em; }

    .widget-footer {
      height: 34px; display: flex; align-items: center; justify-content: space-between;
      gap: 8px; font-size: 11px; color: var(--muted); flex-shrink: 0;
    }
    .widget-footer > div { display: flex; gap: 14px; }
    .widget-footer button {
      font-size: 12px; background: none; border: 0; padding: 4px 0; color: var(--accent);
      text-decoration: underline; text-underline-offset: 3px;
    }

    .card {
      background: var(--card); border: 1px solid var(--line);
      border-radius: 8px; padding: 14px 16px; margin-bottom: 14px;
    }
    .card h2 { margin: 0 0 10px; font-size: 1rem; }
    table { width: 100%; border-collapse: collapse; font-size: .86rem; }
    th, td { text-align: left; padding: 8px 10px; border-bottom: 1px solid var(--line); }
    th { color: var(--muted); font-weight: 700; font-size: .78rem; text-transform: uppercase; }
    @media (max-width: 640px) {
      .branch-list { grid-template-columns: 1fr; }
    }
  </style>
</head>
<body>
  <header class="topbar">
    <div>
      <h1>__TITLE__</h1>
      <div class="sub" id="updated">Loading telemetry…</div>
    </div>
    <div class="topbar-actions">
      <button class="btn-top active" data-tab="graph" onclick="switchTab('graph')">FPKI Graph by Policy</button>
      <button class="btn-top" data-tab="metrics" onclick="switchTab('metrics')">Telemetry &amp; Metrics</button>
      <button class="btn-top" onclick="refreshAll(true)">Refresh Now</button>
    </div>
  </header>

  <main class="wrap">
    <section class="kpi-strip">
      <div class="kpi"><div class="kpi-label">Readiness</div><div class="kpi-val" id="readiness">-</div></div>
      <div class="kpi"><div class="kpi-label">Liveness</div><div class="kpi-val" id="liveness">-</div></div>
      <div class="kpi"><div class="kpi-label">Policies</div><div class="kpi-val" id="policiesCount">-</div></div>
      <div class="kpi"><div class="kpi-label">Intermediates</div><div class="kpi-val" id="intermediatesCount">-</div></div>
      <div class="kpi"><div class="kpi-label">Cache Entries</div><div class="kpi-val" id="cacheEntries">-</div></div>
      <div class="kpi"><div class="kpi-label">Cache Hit Ratio</div><div class="kpi-val" id="hitRatio">-</div></div>
      <div class="kpi"><div class="kpi-label">Hits / Misses</div><div class="kpi-val" id="hitsMisses" style="font-size:1rem">-</div></div>
    </section>

    <section id="tab-graph">
      <div class="policy-bar">
        <div class="policy-pills" id="policyPills">Loading policies…</div>
        <div class="policy-meta" id="policyMeta"></div>
      </div>

      <div class="fpki-shell" id="fpkiShell">
        <div class="fpki-bar">
          <strong id="shellTitle">Certificate relationships</strong>
          <button type="button" id="expandBtn" onclick="toggleExpand()">Expand graph ↗</button>
        </div>

        <div class="widget">
          <div class="toolbar">
            <form id="searchForm" role="search">
              <span class="search-symbol" aria-hidden="true">⌕</span>
              <input type="search" id="searchInput" placeholder="Find an authority, e.g. Treasury, WidePoint, or DoD" autocomplete="off" />
              <button type="submit">Find</button>
            </form>
            <button type="button" id="browseBtn" onclick="showSearch(true)">Browse</button>
          </div>

          <nav class="navigation" aria-label="Graph navigation">
            <div class="nav-buttons">
              <button type="button" id="backBtn" disabled onclick="goBack()">← Back</button>
              <button type="button" id="wholeBtn" onclick="goWhole()">Whole network</button>
              <button type="button" id="excludedBtn" onclick="showExcluded()">Excluded (<span id="excludedCount">0</span>)</button>
            </div>
            <span class="direction">Issuer <span aria-hidden="true">→</span> Issued to</span>
          </nav>

          <section id="searchResults" class="search-results" hidden>
            <div class="panel-head">
              <h2 id="resultHeading">Authorities</h2>
              <button type="button" onclick="closePanels()">Close</button>
            </div>
            <ul id="resultList"></ul>
          </section>

          <section id="detailsPanel" class="details-panel" hidden>
            <div class="panel-head">
              <h2 id="detailsHeading">Authority details</h2>
              <button type="button" onclick="closePanels()">Close details</button>
            </div>
            <div id="detailsBody"></div>
          </section>

          <section id="excludedPanel" class="excluded-panel" hidden>
            <div class="panel-head">
              <h2 id="excludedHeading">Excluded authorities</h2>
              <button type="button" onclick="closePanels()">Close</button>
            </div>
            <div id="excludedBody"></div>
          </section>

          <section id="surface" class="surface">
            <div style="padding:24px;color:var(--muted)">Loading certificate relationships…</div>
          </section>

          <footer class="widget-footer">
            <span id="graphSource">Graph source: loading…</span>
            <div>
              <button type="button" onclick="showDetails()">Details</button>
            </div>
          </footer>
        </div>
      </div>
    </section>

    <section id="tab-metrics" hidden>
      <div class="card">
        <h2>Actuator &amp; Micrometer Telemetry</h2>
        <table>
          <thead><tr><th>Metric</th><th>Value</th></tr></thead>
          <tbody id="metricsTable"></tbody>
        </table>
      </div>
    </section>
  </main>

  <div id="tooltip" class="node-tooltip" hidden></div>

  <script>
    const BRANCH_COLORS = ['#39728b','#438e88','#927497','#af8649','#5c82a5','#718b64','#ac7866','#747da5','#60959f','#a28c69','#7d8e91'];
    const FPKI_OID_NAMES = {
      '2.5.29.32.0': 'anyPolicy',
      '2.16.840.1.101.3.2.1.3.1': 'id-fpki-fbca-rudimentary',
      '2.16.840.1.101.3.2.1.3.2': 'id-fpki-fbca-basic',
      '2.16.840.1.101.3.2.1.3.3': 'id-fpki-fbca-medium',
      '2.16.840.1.101.3.2.1.3.4': 'id-fpki-fbca-high',
      '2.16.840.1.101.3.2.1.3.6': 'id-fpki-common-policy',
      '2.16.840.1.101.3.2.1.3.7': 'id-fpki-common-authentication',
      '2.16.840.1.101.3.2.1.3.8': 'id-fpki-common-signing',
      '2.16.840.1.101.3.2.1.3.12': 'id-fpki-fbca-mediumHardware',
      '2.16.840.1.101.3.2.1.3.13': 'id-fpki-common-hardware',
      '2.16.840.1.101.3.2.1.3.14': 'id-fpki-fbca-mediumCBP',
      '2.16.840.1.101.3.2.1.3.15': 'id-fpki-fbca-mediumHardwareCBP',
      '2.16.840.1.101.3.2.1.3.16': 'id-fpki-common-high',
      '2.16.840.1.101.3.2.1.3.17': 'id-fpki-common-cardAuth',
      '2.16.840.1.101.3.2.1.3.18': 'id-fpki-common-derived-pivAuth',
      '2.16.840.1.101.3.2.1.3.19': 'id-fpki-common-derived-pivAuth-hardware',
      '2.16.840.1.101.3.2.1.3.24': 'id-fpki-fbca-medium-Device',
      '2.16.840.1.101.3.2.1.3.25': 'id-fpki-fbca-mediumHardware-Device',
      '2.16.840.1.101.3.2.1.3.37': 'id-fpki-fbca-pivi-hardware',
      '2.16.840.1.101.3.2.1.3.38': 'id-fpki-fbca-pivi-cardAuth',
      '2.16.840.1.101.3.2.1.3.39': 'id-fpki-common-pivi-auth',
      '2.16.840.1.101.3.2.1.3.40': 'id-fpki-common-pivi-contentSigning',
      '2.16.840.1.101.3.2.1.3.41': 'id-fpki-common-pivi-hardware',
      '2.16.840.1.101.3.2.1.3.44': 'id-fpki-common-pivi-cardAuth',
      '2.16.840.1.101.3.2.1.3.45': 'id-fpki-common-devices',
      '2.16.840.1.101.3.2.1.3.46': 'id-fpki-common-devicesHardware',
      '2.16.840.1.101.3.2.1.3.48': 'id-fpki-fbca-contentSigning',
      '2.16.840.1.101.3.2.1.3.49': 'id-fpki-common-piv-contentSigning',
      '2.16.840.1.101.3.2.1.3.51': 'id-fpki-common-derived-pivAuth-cardAuth'
    };

    let graphData = null;
    let activePolicyId = null;
    let model = null;
    let owners = new Map();
    let viewState = { ca: null, view: 'overview' };
    let historyTrail = [];
    let activePanel = '';

    const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
    const quantity = (n, singular, plural) => `${n} ${n === 1 ? singular : (plural || singular + 's')}`;
    const oidLabel = oid => FPKI_OID_NAMES[oid] || '';

    function renderOidChip(oid, cls = '') {
      const name = oidLabel(oid);
      return `<span class="oid-chip ${cls}" title="${esc(oid)}"><code>${esc(oid)}</code>${name ? `<span>${esc(name)}</span>` : ''}</span>`;
    }

    function renderPolicyOidBreakdownHTML(key, compact = false) {
      if (!graphData || !model) return '';
      const node = model.nodes.get(key) || (graphData.nodes || {})[key];
      if (!node) return '';
      const asserted = node.certificatePolicies || [];
      const activePol = activePolicyObj();
      const activeValid = new Set((activePol?.validPolicyOids || {})[key] || []);
      const assertedHTML = asserted.length
        ? asserted.map(o => renderOidChip(o, activeValid.has(o) ? 'valid' : '')).join('')
        : '<span style="color:var(--muted)">None asserted (implicit/unconstrained or root)</span>';

      const rows = (graphData.policies || []).map(p => {
        const reach = (p.reachableNodes || []).includes(key);
        const excl = (p.excludedForPolicy || []).find(e => e.id === key);
        const validOids = (p.validPolicyOids || {})[key] || [];
        const leaves = (p.policyLeaves || {})[key] || [];
        let oidsCell = '';
        if (!reach) {
          oidsCell = `<span style="color:var(--bad)">Excluded: ${esc(excl ? excl.reason : 'Not reachable under policy')}</span>`;
        } else if (leaves.length) {
          oidsCell = `<div class="oid-chips">${leaves.map(l => {
            const vp = l.validPolicy;
            const exp = (l.expectedPolicies || []).filter(e => e !== vp);
            const name = oidLabel(vp);
            const mapTxt = exp.length ? ` ← mapped [${exp.map(e => oidLabel(e) || e).join(', ')}]` : '';
            return `<span class="oid-chip ${exp.length ? 'mapped' : 'valid'}"><code>${esc(vp)}</code>${name ? `<span>${esc(name)}</span>` : ''}${mapTxt ? `<span style="opacity:.85">${esc(mapTxt)}</span>` : ''}</span>`;
          }).join('')}</div>`;
        } else if (validOids.length) {
          oidsCell = `<div class="oid-chips">${validOids.map(o => renderOidChip(o, 'valid')).join('')}</div>`;
        } else {
          oidsCell = '<span style="color:var(--muted)">No matching valid policy OIDs</span>';
        }
        return `<tr>
          <td><strong>${esc(p.validationPolicyName)}</strong><br><code style="font-size:10px;color:var(--muted)">${esc(p.validationPolicyId)}</code></td>
          <td>${reach ? badge('VALID') : '<span class="badge down">EXCLUDED</span>'}</td>
          <td>${oidsCell}</td>
        </tr>`;
      }).join('');

      return `<div class="oid-breakdown">
        <div class="oid-breakdown-head">
          <span>Certificate Policy OIDs · ${esc(node.label)}</span>
          <span style="font-size:11px;font-weight:400;color:var(--muted)">Green = valid under active policy (${esc(activePol?.validationPolicyName || '')}) · Blue = mapped via policyMapping</span>
        </div>
        <div style="font-size:11px;color:var(--muted);margin-bottom:4px"><strong>Asserted in Certificate Extension (2.5.29.32):</strong></div>
        <div class="oid-chips">${assertedHTML}</div>
        ${compact ? `<details><summary style="cursor:pointer;font-weight:700;color:var(--accent)">Valid Policy OIDs per Cached Validation Policy (${(graphData.policies || []).length})</summary><table class="oid-matrix" style="margin-top:6px"><thead><tr><th>Validation Policy</th><th>Status</th><th>Valid Policy OIDs (PKIX Policy Tree)</th></tr></thead><tbody>${rows}</tbody></table></details>`
          : `<div style="font-size:11px;color:var(--muted);margin:8px 0 4px"><strong>Valid Policy OIDs per Cached Validation Policy:</strong></div><table class="oid-matrix"><thead><tr><th>Validation Policy</th><th>Status</th><th>Valid Policy OIDs (PKIX Policy Tree)</th></tr></thead><tbody>${rows}</tbody></table>`}
      </div>`;
    }

    function switchTab(tab) {
      ['graph', 'metrics'].forEach(t => {
        document.getElementById('tab-' + t).hidden = t !== tab;
      });
      document.querySelectorAll('.btn-top[data-tab]').forEach(b => {
        b.classList.toggle('active', b.dataset.tab === tab);
      });
      if (tab === 'graph' && model) renderSurface();
    }

    function toggleExpand() {
      const shell = document.getElementById('fpkiShell');
      const expanded = shell.classList.toggle('expanded');
      document.getElementById('expandBtn').textContent = expanded ? 'Close expanded view ✕' : 'Expand graph ↗';
      if (model) renderSurface();
    }

    function badge(status) {
      return `<span class="badge ${status === 'UP' || status === 'VALID' ? 'up' : 'down'}">${esc(status)}</span>`;
    }

    async function refreshStatus() {
      try {
        const res = await fetch('./api/status', { cache: 'no-store' });
        const d = await res.json();
        if (d.error) throw new Error(d.error);
        const m = d.metrics || {};
        const hits = Number(m['vss.validation.cache.hits'] || 0);
        const misses = Number(m['vss.validation.cache.misses'] || 0);
        const total = hits + misses;
        const pct = total > 0 ? ((hits / total) * 100).toFixed(1) + '%' : 'n/a';

        document.getElementById('readiness').innerHTML = badge(d.readiness);
        document.getElementById('liveness').innerHTML = badge(d.liveness);
        document.getElementById('policiesCount').textContent = Math.round(m['vss.policies.loaded'] || 0);
        document.getElementById('intermediatesCount').textContent = Math.round(m['vss.intermediates.cached'] || 0);
        document.getElementById('cacheEntries').textContent = Math.round(m['vss.validation.cache.entries'] || 0);
        document.getElementById('hitRatio').textContent = pct;
        document.getElementById('hitsMisses').textContent = `${hits.toLocaleString()} / ${misses.toLocaleString()}`;
        document.getElementById('metricsTable').innerHTML = Object.entries(m)
          .map(([k, v]) => `<tr><td><code>${esc(k)}</code></td><td>${esc(v)}</td></tr>`).join('');
        document.getElementById('updated').textContent = `Live · Auto-refresh 5s · Updated ${new Date().toLocaleTimeString()}`;
      } catch (e) {
        document.getElementById('updated').textContent = 'Status error: ' + e.message;
      }
    }

    async function refreshGraph(force = false) {
      const url = force ? './api/graph?refresh=1' : './api/graph';
      const res = await fetch(url, { cache: 'no-store' });
      const d = await res.json();
      if (d.error) {
        document.getElementById('surface').innerHTML = `<div style="padding:20px;color:#b42318">Graph error: ${esc(d.error)}</div>`;
        return;
      }
      graphData = d;
      const policies = d.policies || [];
      if (!activePolicyId || !policies.some(p => p.validationPolicyId === activePolicyId)) {
        activePolicyId = policies[0]?.validationPolicyId || null;
      }
      renderPolicyBar();
      buildModelForActivePolicy();
      document.getElementById('graphSource').textContent =
        `Telemetry: ${d.telemetrySource || 'actuator/vssgraph'} · ${d.nodeCount || 0} total CAs · ${d.edgeCount || 0} verified links`;
    }

    async function refreshAll(forceGraph = false) {
      await Promise.all([refreshStatus(), refreshGraph(forceGraph)]);
    }

    function activePolicyObj() {
      return (graphData?.policies || []).find(p => p.validationPolicyId === activePolicyId) || null;
    }

    function renderPolicyBar() {
      const policies = graphData?.policies || [];
      document.getElementById('policyPills').innerHTML = policies.map(p => {
        const sel = p.validationPolicyId === activePolicyId ? 'selected' : '';
        const reach = (p.reachableNodes || []).length;
        const excl = (p.excludedForPolicy || []).length;
        return `<button type="button" class="policy-pill ${sel}" onclick="selectPolicy('${esc(p.validationPolicyId)}')">
          <strong>${esc(p.validationPolicyName)}</strong> · ${reach} CAs${excl ? ` (${excl} excluded)` : ''}
          <span class="pid">${esc(p.validationPolicyId)}</span>
        </button>`;
      }).join('');

      const p = activePolicyObj();
      if (!p) return;
      const oids = (p.userPolicySet || []).map(o => `<code>${esc(o)}</code>`).join(' ');
      document.getElementById('policyMeta').innerHTML = `
        <span>${esc(p.validationPolicyDescription || '')}</span>
        <span><strong>userPolicySet:</strong> ${oids || 'none'}</span>
        <span><strong>Flags:</strong> explicit=${p.requireExplicitPolicy} · inhibitMapping=${p.inhibitPolicyMapping} · inhibitAny=${p.inhibitAnyPolicy}</span>
      `;
    }

    function selectPolicy(pid) {
      activePolicyId = pid;
      historyTrail = [];
      activePanel = '';
      renderPolicyBar();
      buildModelForActivePolicy();
    }

    function buildModelForActivePolicy() {
      const p = activePolicyObj();
      if (!p || !graphData) return;
      const allNodes = graphData.nodes || {};
      const reachSet = new Set(p.reachableNodes || []);
      const nodes = new Map();
      const outgoing = new Map();
      const incoming = new Map();

      for (const id of reachSet) {
        if (allNodes[id]) {
          nodes.set(id, allNodes[id]);
          outgoing.set(id, []);
          incoming.set(id, []);
        }
      }
      const edges = (p.edges || []).filter(e => nodes.has(e.source) && nodes.has(e.target));
      for (const e of edges) {
        outgoing.get(e.source).push(e.target);
        incoming.get(e.target).push(e.source);
      }
      for (const [, list] of outgoing) {
        list.sort((a, b) => (nodes.get(a).label || '').localeCompare(nodes.get(b).label || ''));
      }
      for (const [, list] of incoming) {
        list.sort((a, b) => (nodes.get(a).label || '').localeCompare(nodes.get(b).label || ''));
      }

      const anchor = (p.anchors && p.anchors.find(a => nodes.has(a))) || [...nodes.keys()][0] || null;
      model = { nodes, edges, outgoing, incoming, anchor, policy: p };

      owners = new Map();
      if (anchor) {
        const tree = rootedGraph(anchor);
        tree.branches.forEach((b, i) => owners.set(b, BRANCH_COLORS[i % BRANCH_COLORS.length]));
        for (const k of tree.keys) {
          if (k !== anchor) owners.set(k, owners.get(tree.owner.get(k)) || BRANCH_COLORS[0]);
        }
      }

      viewState = { ca: anchor, view: 'overview' };
      const exclCount = (p.excludedForPolicy || []).length + (graphData.excludedFromBundle || []).length;
      document.getElementById('excludedCount').textContent = exclCount;
      updateNavButtons();
      closePanels();
      renderSurface();
    }

    function rootedGraph(root) {
      const children = new Map([[root, []]]), parent = new Map(), depth = new Map([[root, 0]]), keys = [root], owner = new Map();
      for (let i = 0; i < keys.length; i++) {
        const k = keys[i];
        for (const child of (model.outgoing.get(k) || [])) {
          if (depth.has(child)) continue;
          keys.push(child);
          depth.set(child, depth.get(k) + 1);
          parent.set(child, k);
          children.get(k).push(child);
          children.set(child, []);
          owner.set(child, k === root ? child : owner.get(k));
        }
      }
      const visible = new Set(keys);
      const edges = model.edges.filter(e => visible.has(e.source) && visible.has(e.target));
      const leafWeight = new Map();
      for (const k of [...keys].reverse()) {
        const ch = children.get(k);
        leafWeight.set(k, ch.length ? ch.reduce((s, c) => s + leafWeight.get(c), 0) : 1);
      }
      const angle = new Map([[root, -Math.PI / 2]]);
      const ranges = new Map([[root, [-Math.PI / 2, Math.PI * 1.5]]]);
      for (const k of keys) {
        const list = children.get(k);
        if (!list.length) continue;
        const [start, end] = ranges.get(k);
        const weight = list.reduce((s, c) => s + leafWeight.get(c), 0) || 1;
        let cursor = start;
        for (const child of list) {
          const equal = k === root ? 0.6 : 0.16;
          const span = (end - start) * (equal / list.length + (1 - equal) * leafWeight.get(child) / weight);
          ranges.set(child, [cursor, cursor + span]);
          angle.set(child, cursor + span / 2);
          cursor += span;
        }
      }
      return {
        root, children, parent, depth, keys, edges, owner, angle,
        branches: children.get(root),
        maxDepth: Math.max(0, ...depth.values()),
      };
    }

    function routeToAnchor(target) {
      if (!target || !model.anchor) return [];
      if (target === model.anchor) return [model.anchor];
      const q = [model.anchor];
      const prev = new Map([[model.anchor, null]]);
      while (q.length) {
        const cur = q.shift();
        if (cur === target) break;
        for (const nxt of (model.outgoing.get(cur) || [])) {
          if (!prev.has(nxt)) {
            prev.set(nxt, cur);
            q.push(nxt);
          }
        }
      }
      if (!prev.has(target)) return [target];
      const path = [];
      for (let cur = target; cur !== null; cur = prev.get(cur)) path.push(cur);
      return path.reverse();
    }

    function radialCurve(s, t, cx, cy) {
      const shorten = (n, toward, dist) => {
        const dx = toward.x - n.x, dy = toward.y - n.y, len = Math.hypot(dx, dy) || 1;
        return { x: n.x + dx * dist / len, y: n.y + dy * dist / len };
      };
      const sp = shorten(s, t, s.depth === 0 ? 22 : s.number ? 10 : 3.8);
      const tp = shorten(t, s, t.depth === 0 ? 22 : t.number ? 11 : 4.5);
      if (s.depth === 0) {
        return `M${sp.x},${sp.y} C${sp.x + (tp.x - sp.x) / 3},${sp.y + (tp.y - sp.y) / 3} ${sp.x + (tp.x - sp.x) * 2 / 3},${sp.y + (tp.y - sp.y) * 2 / 3} ${tp.x},${tp.y}`;
      }
      const mid = (s.r + t.r) / 2;
      return `M${sp.x},${sp.y} C${cx + Math.cos(s.angle) * mid},${cy + Math.sin(s.angle) * mid} ${cx + Math.cos(t.angle) * mid},${cy + Math.sin(t.angle) * mid} ${tp.x},${tp.y}`;
    }

    function renderSurface() {
      const surface = document.getElementById('surface');
      if (!model || !model.anchor) {
        surface.innerHTML = '<div style="padding:24px;color:var(--muted)">No authorities loaded for this policy.</div>';
        return;
      }

      const selected = viewState.view === 'overview' ? model.anchor : viewState.ca;
      const node = model.nodes.get(selected);
      const outgoing = model.outgoing.get(selected) || [];
      const incoming = model.incoming.get(selected) || [];
      const isLeaf = outgoing.length === 0;
      const root = isLeaf && incoming.length ? incoming[0] : selected;
      const tree = rootedGraph(root);
      if (isLeaf) {
        tree.keys = tree.keys.filter(k => tree.depth.get(k) <= 1);
        const vis = new Set(tree.keys);
        tree.edges = tree.edges.filter(e => vis.has(e.source) && vis.has(e.target));
        tree.maxDepth = Math.min(1, tree.maxDepth);
      }

      const width = Math.max(320, surface.clientWidth - 4);
      const heightAvail = Math.max(360, surface.clientHeight - 210);
      const labelWidth = Math.max(155, Math.min(255, width * 0.24));
      const branchMeta = tree.branches.map((k, idx) => ({
        key: k, number: idx + 1, angle: tree.angle.get(k), label: model.nodes.get(k).label
      }));
      const leftLabels = branchMeta.filter(b => Math.cos(b.angle) < 0).sort((a, b) => Math.sin(a.angle) - Math.sin(b.angle));
      const rightLabels = branchMeta.filter(b => Math.cos(b.angle) >= 0).sort((a, b) => Math.sin(a.angle) - Math.sin(b.angle));
      const flanked = width >= 660 && Math.max(leftLabels.length, rightLabels.length) * 34 <= heightAvail + 40;

      const svgHeight = tree.keys.length === 1 ? 180 : flanked
        ? Math.max(360, heightAvail)
        : Math.min(390, Math.max(260, width * 0.68));
      const radius = flanked
        ? Math.max(85, Math.min((width - labelWidth * 2 - 44) / 2, (svgHeight - 44) / 2))
        : Math.min(width * 0.4, (svgHeight - 46) / 2);
      const cx = width / 2, cy = svgHeight / 2;

      const pos = new Map();
      const branchNumbers = new Map(branchMeta.map(b => [b.key, b.number]));
      for (const k of tree.keys) {
        const d = tree.depth.get(k);
        const ang = tree.angle.get(k);
        const r = tree.maxDepth === 0 ? 0 : d === 0 ? 0 : radius * (0.28 + 0.72 * (d / tree.maxDepth));
        pos.set(k, {
          x: cx + Math.cos(ang) * r,
          y: cy + Math.sin(ang) * r,
          r, angle: ang, depth: d,
          number: branchNumbers.get(k) || 0,
        });
      }

      const pathHTML = routeToAnchor(selected).map((k, i) => {
        const lbl = esc(model.nodes.get(k).label);
        return (i ? '<span aria-hidden="true">→</span>' : '') +
          (k === selected ? `<span>${lbl}</span>` : `<button type="button" onclick="selectAuthority('${k}')">${lbl}</button>`);
      }).join('');

      const issuersHTML = incoming.length
        ? incoming.map(i => `<button type="button" class="issuer-node" onclick="selectAuthority('${i}')"><span class="issuer-dot">↑</span>${esc(model.nodes.get(i).label)}</button>`).join('')
        : (selected === model.anchor ? 'Policy Trust Anchor (Root)' : 'No issuer in cached set');

      const ringsSVG = Array.from({ length: tree.maxDepth }, (_, i) => {
        const rr = radius * (0.28 + 0.72 * ((i + 1) / tree.maxDepth));
        return `<circle class="ring" cx="${cx}" cy="${cy}" r="${rr.toFixed(1)}" />`;
      }).join('');

      const edgesSVG = tree.edges.map(e => {
        const s = pos.get(e.source), t = pos.get(e.target);
        if (!s || !t) return '';
        const col = owners.get(e.target) || owners.get(e.source) || '#39728b';
        return `<path class="radial-edge" d="${radialCurve(s, t, cx, cy)}" stroke="${col}" marker-end="url(#arrow)" />`;
      }).join('');

      const pointsSVG = tree.keys.map(k => {
        const p = pos.get(k);
        const col = owners.get(k) || '#39728b';
        const isRoot = p.depth === 0;
        const isSel = k === selected && viewState.view === 'focus';
        const r = isRoot ? 22 : p.number ? 10 : 3.8;
        const fill = isRoot ? '#fff8e8' : col;
        const stroke = isRoot ? '#b4975e' : '#ffffff';
        const selClass = isSel ? 'selected-point' : '';
        return `<g class="node-pt" data-id="${k}">
          <circle class="point-circle ${isRoot ? 'root-mark' : ''} ${selClass}" cx="${p.x.toFixed(1)}" cy="${p.y.toFixed(1)}" r="${r}" fill="${fill}" stroke="${stroke}" />
          <circle class="point-hit" cx="${p.x.toFixed(1)}" cy="${p.y.toFixed(1)}" r="${Math.max(12, r + 4)}"
            onclick="selectAuthority('${k}')" onmouseenter="showTip(event,'${k}')" onmouseleave="hideTip()" />
          ${isRoot ? `<text class="root-label" x="${p.x.toFixed(1)}" y="${(p.y + 3).toFixed(1)}" text-anchor="middle">${k === model.anchor ? 'ANCHOR' : 'FOCUS'}</text>` : ''}
          ${p.number ? `<text class="point-number" x="${p.x.toFixed(1)}" y="${(p.y + 3.2).toFixed(1)}" text-anchor="middle" fill="#fff">${p.number}</text>` : ''}
        </g>`;
      }).join('');

      let labelsHTML = '';
      if (flanked) {
        const placeSide = (items, isRight) => {
          const step = items.length ? Math.min(34, (svgHeight - 16) / items.length) : 34;
          const startY = Math.max(6, (svgHeight - items.length * step) / 2);
          return items.map((b, idx) => {
            const top = startY + idx * step;
            const x = isRight ? (cx + radius + 16) : Math.max(8, cx - radius - labelWidth - 16);
            const col = owners.get(b.key) || '#39728b';
            return `<button type="button" class="branch-label" style="--branch-color:${col};left:${x.toFixed(0)}px;top:${top.toFixed(0)}px;width:${labelWidth}px"
              onclick="selectAuthority('${b.key}')" onmouseenter="showTip(event,'${b.key}')" onmouseleave="hideTip()">
              <span class="number">${b.number}</span><span>${esc(b.label)}</span>
            </button>`;
          }).join('');
        };
        labelsHTML = placeSide(leftLabels, false) + placeSide(rightLabels, true);
      }

      const listHTML = !flanked && branchMeta.length ? `<div class="branch-list">${branchMeta.map(b => {
        const col = owners.get(b.key) || '#39728b';
        return `<button type="button" class="branch-label" style="--branch-color:${col}" onclick="selectAuthority('${b.key}')">
          <span class="number">${b.number}</span><span>${esc(b.label)}</span>
        </button>`;
      }).join('')}</div>` : '';

      surface.innerHTML = `
        <div class="graph-context">
          <nav class="context-path">${pathHTML}</nav>
          <h2 class="surface-title">${esc(node.label)}</h2>
          <p class="surface-caption">Policy: <strong>${esc(model.policy.validationPolicyName)}</strong> · Shown: ${quantity(tree.keys.length, 'authority', 'authorities')} · ${quantity(tree.edges.length, 'directed relationship')}${isLeaf && incoming.length ? ' · includes issuer and peers' : ''}</p>
          <div class="authority-about">
            ${node.affiliation ? `<p class="authority-affiliation">Affiliation: ${esc(node.affiliation)}</p>` : ''}
            <p>Certificate issuer: ${issuersHTML}</p>
          </div>
          ${renderPolicyOidBreakdownHTML(selected, true)}
        </div>
        <div class="radial-wrap" style="height:${svgHeight}px">
          <svg class="radial-svg" width="${width}" height="${svgHeight}" viewBox="0 0 ${width} ${svgHeight}">
            <defs>
              <marker id="arrow" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="5.5" markerHeight="5.5" orient="auto-start-reverse">
                <path d="M 0 1.5 L 8 5 L 0 8.5 z" fill="#526e7c" opacity="0.7" />
              </marker>
            </defs>
            ${ringsSVG}
            ${edgesSVG}
            ${pointsSVG}
          </svg>
          ${flanked ? labelsHTML : ''}
        </div>
        ${listHTML}
      `;
    }

    function showTip(evt, key) {
      if (!model || !model.nodes.has(key)) return;
      const n = model.nodes.get(key);
      const tip = document.getElementById('tooltip');
      const kids = (model.outgoing.get(key) || []).length;
      tip.innerHTML = `<strong>${esc(n.label)}</strong><br><span style="opacity:.8">Issued to ${kids} CA(s) · Expires ${esc((n.notAfter || '').slice(0, 10))}</span>`;
      tip.style.left = Math.min(window.innerWidth - 310, evt.clientX + 12) + 'px';
      tip.style.top = Math.min(window.innerHeight - 70, evt.clientY + 12) + 'px';
      tip.hidden = false;
    }
    function hideTip() { document.getElementById('tooltip').hidden = true; }

    function selectAuthority(key) {
      if (!model || !model.nodes.has(key)) return;
      hideTip();
      historyTrail.push({ ...viewState });
      viewState = { ca: key, view: 'focus' };
      closePanels();
      updateNavButtons();
      renderSurface();
    }

    function goBack() {
      if (!historyTrail.length) return;
      viewState = historyTrail.pop();
      closePanels();
      updateNavButtons();
      renderSurface();
    }

    function goWhole() {
      if (!model) return;
      historyTrail.push({ ...viewState });
      viewState = { ca: model.anchor, view: 'overview' };
      closePanels();
      updateNavButtons();
      renderSurface();
    }

    function updateNavButtons() {
      document.getElementById('backBtn').disabled = historyTrail.length === 0;
    }

    function closePanels() {
      activePanel = '';
      document.getElementById('searchResults').hidden = true;
      document.getElementById('detailsPanel').hidden = true;
      document.getElementById('excludedPanel').hidden = true;
      document.getElementById('surface').hidden = false;
    }

    function setPanel(name) {
      activePanel = name;
      document.getElementById('searchResults').hidden = name !== 'search';
      document.getElementById('detailsPanel').hidden = name !== 'details';
      document.getElementById('excludedPanel').hidden = name !== 'excluded';
      document.getElementById('surface').hidden = !!name;
    }

    function showSearch(browseAll = false) {
      if (!model) return;
      const q = browseAll ? '' : document.getElementById('searchInput').value.trim().toLowerCase();
      const keys = [...model.nodes.keys()]
        .filter(k => {
          if (!q) return true;
          const n = model.nodes.get(k);
          return [n.label, n.subject, n.affiliation, n.id].some(v => (v || '').toLowerCase().includes(q));
        })
        .sort((a, b) => (model.nodes.get(a).label || '').localeCompare(model.nodes.get(b).label || ''));
      document.getElementById('resultHeading').textContent = browseAll
        ? `All ${keys.length} authorities in policy "${model.policy.validationPolicyName}"`
        : `${keys.length} matching ${keys.length === 1 ? 'authority' : 'authorities'}`;
      document.getElementById('resultList').innerHTML = keys.length ? keys.map(k => {
        const n = model.nodes.get(k);
        const iss = model.incoming.get(k) || [];
        const ctx = iss.length ? 'Issued by ' + iss.map(i => model.nodes.get(i).label).join(' · ')
          : (k === model.anchor ? 'Policy Trust Anchor' : 'Intermediate CA');
        return `<li><button type="button" onclick="selectAuthority('${k}')">${esc(n.label)}<span>${esc(ctx)}</span></button></li>`;
      }).join('') : '<li><p style="padding:14px;color:var(--muted)">No matching authorities.</p></li>';
      setPanel('search');
    }

    function showDetails() {
      if (!model || !viewState.ca) return;
      const k = viewState.ca;
      const n = model.nodes.get(k);
      const iss = model.incoming.get(k) || [];
      const out = model.outgoing.get(k) || [];
      const btnList = ids => ids.length
        ? `<div class="rel-list">${ids.map(id => `<button type="button" onclick="selectAuthority('${id}')">${esc(model.nodes.get(id).label)}</button>`).join('')}</div>`
        : '<span style="color:var(--muted)">None</span>';
      document.getElementById('detailsHeading').textContent = n.label;
      document.getElementById('detailsBody').innerHTML = `
        ${renderPolicyOidBreakdownHTML(k, false)}
        <dl>
          <dt>Subject DN</dt><dd>${esc(n.subject)}</dd>
          <dt>Issuer DN</dt><dd>${esc(n.issuer)}</dd>
          <dt>x5t#S256 (Thumbprint)</dt><dd>${esc(n.id)}</dd>
          <dt>Serial Number (Hex)</dt><dd>${esc(n.serialNumber)}</dd>
          <dt>Validity</dt><dd>${esc(n.notBefore)} → ${esc(n.notAfter)}</dd>
          <dt>Key &amp; Signature</dt><dd>${esc(n.publicKey)} · ${esc(n.sigAlgName)}</dd>
          <dt>Basic Constraints (pathLen)</dt><dd>${n.basicConstraints}</dd>
          <dt>Key Usage</dt><dd>${esc((n.keyUsage || []).join(', ') || 'n/a')}</dd>
          <dt>Certificate Policy OIDs</dt><dd>${esc((n.certificatePolicies || []).join('\n') || 'none')}</dd>
          <dt>AIA CA Issuers</dt><dd>${esc((n.aiaCaIssuers || []).join('\n') || 'none')}</dd>
          <dt>OCSP Endpoints</dt><dd>${esc((n.ocspUrls || []).join('\n') || 'none')}</dd>
          <dt>SIA CA Repository</dt><dd>${esc((n.siaCaRepository || []).join('\n') || 'none')}</dd>
          <dt>CRL Distribution Points</dt><dd>${esc((n.crlDpUrls || []).join('\n') || 'none')}</dd>
        </dl>
        <h3>Issued By</h3>${btnList(iss)}
        <h3>Issued To (${out.length})</h3>${btnList(out)}
      `;
      setPanel('details');
    }

    function showExcluded() {
      if (!model) return;
      const polExcl = model.policy.excludedForPolicy || [];
      const bdlExcl = graphData.excludedFromBundle || [];
      document.getElementById('excludedHeading').textContent = `Excluded authorities · Policy "${model.policy.validationPolicyName}"`;
      document.getElementById('excludedBody').innerHTML = `
        <h3 style="margin-top:0;border-top:0;padding-top:0">Not Valid Under Policy "${esc(model.policy.validationPolicyName)}" (${polExcl.length})</h3>
        ${polExcl.length ? `<table><thead><tr><th>Authority</th><th>x5t#S256</th><th>PKIX Rejection Reason</th></tr></thead><tbody>
          ${polExcl.map(e => `<tr><td><strong>${esc(e.label)}</strong></td><td><code>${esc(e.id)}</code></td><td>${esc(e.reason)}</td></tr>`).join('')}
        </tbody></table>` : '<p style="color:var(--muted)">All cached intermediates validate under this policy.</p>'}
        <h3>Excluded During Bundle Load (${bdlExcl.length})</h3>
        ${bdlExcl.length ? `<table><thead><tr><th>Subject</th><th>x5t#S256</th><th>Reason</th></tr></thead><tbody>
          ${bdlExcl.map(e => `<tr><td>${esc(e.x509SubjectName)}</td><td><code>${esc(e.x5tS256)}</code></td><td>${esc(e.excludeReason)}</td></tr>`).join('')}
        </tbody></table>` : '<p style="color:var(--muted)">No bundle-level exclusions recorded.</p>'}
      `;
      setPanel('excluded');
    }

    document.getElementById('searchForm').addEventListener('submit', e => {
      e.preventDefault();
      showSearch(false);
    });
    window.addEventListener('resize', () => { if (model && !activePanel) renderSurface(); });

    refreshAll();
    setInterval(refreshStatus, 5000);
    setInterval(() => refreshGraph(false), 60000);
  </script>
</body>
</html>
"""

HTML_PAGE = HTML_TEMPLATE.replace("__TITLE__", html.escape(DASHBOARD_TITLE))


class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        parsed = urlparse(self.path)
        path = parsed.path
        if path == "/healthz" or path.endswith("/healthz"):
            self._send(200, "application/json", b'{"status":"UP"}')
            return
        if path.endswith("/api/status"):
            payload = json.dumps(collect_status()).encode("utf-8")
            self._send(200, "application/json", payload)
            return
        if path.endswith("/api/graph"):
            qs = parse_qs(parsed.query)
            force = qs.get("refresh", ["0"])[0] == "1"
            payload = json.dumps(collect_graph(force=force)).encode("utf-8")
            self._send(200, "application/json", payload)
            return

        self._send(200, "text/html; charset=utf-8", HTML_PAGE.encode("utf-8"))

    def _send(self, code, ctype, body):
        self.send_response(code)
        self.send_header("Content-Type", ctype)
        self.send_header("Cache-Control", "no-store")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, format, *args):
        pass


if __name__ == "__main__":
    server = ThreadingHTTPServer((BIND_HOST, PORT), Handler)
    server.serve_forever()
