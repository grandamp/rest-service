# VSS Observability & FPKI Graph Container (`observability/`)

Lightweight, unprivileged web UI container for `rest-service` that visualizes:

- **Actuator Health & Micrometer Metrics:** Readiness, liveness, loaded policies, cached intermediates, validation cache entries, and hit/miss ratios.
- **FPKI Graph by Policy:** Interactive concentric-ring radial trust atlas modeled after the [idmanagement.gov FPKI Graph](https://www.idmanagement.gov/fpki/notifications/#fpki-graph), scoped per configured `ValidationPolicy`, with search/browse, focus navigation, authority certificate details, policy-excluded CAs, and per-policy validated Certificate Policy OID breakdowns (`PKIXCertPathValidatorResult.getPolicyTree()`).

## Configuration

| Variable | Default | Description |
| --- | --- | --- |
| `PORT` | `8099` | HTTP listen port inside the container |
| `BIND_HOST` | `0.0.0.0` | Listen address |
| `VSS_BASE_URL` | `http://127.0.0.1:8000` | Base URL of the `rest-service` Actuator HTTP/HTTPS listener |
| `VSS_TLS_VERIFY` | `true` | Set to `false` when pointing `VSS_BASE_URL` at a self-signed HTTPS listener |
| `GRAPH_CACHE_SECONDS` | `60` | Server-side cache TTL for `/actuator/vssgraph` |
| `STATUS_CACHE_SECONDS` | `3` | Server-side cache TTL for `/actuator/health` & `/actuator/metrics` |
| `DASHBOARD_TITLE` | `KeySupport VSS · Observability & FPKI Graph` | Page header and `<title>` |

> **Requirement:** `rest-service` must expose `health,info,metrics,vssgraph` under `management.endpoints.web.exposure.include` (enabled by default in `src/main/resources/application.yml`). Keep Actuator bound to a private interface or reverse proxy only `/observability/` publicly so internal endpoints are not directly exposed.

## Build & Run

```bash
# Build locally
docker build -t vss-observability:latest -f observability/Dockerfile observability

# Run alongside rest-service on port 8099
docker run -d \
  --name vss-observability \
  --restart unless-stopped \
  --network host \
  -e PORT=8099 \
  -e VSS_BASE_URL=http://127.0.0.1:8000 \
  vss-observability:latest
```

## Reverse Proxy Integration (Nginx)

Because the UI uses relative `./api/status` and `./api/graph` requests, it can be mounted at `/` or under a subpath such as `/observability/`:

```nginx
upstream vss_observability {
    server 127.0.0.1:8099;
    keepalive 16;
}

location = /observability {
    return 301 /observability/;
}

location /observability/ {
    proxy_pass http://vss_observability/;
    proxy_http_version 1.1;
    proxy_set_header Connection "";
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```
