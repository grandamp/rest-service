# Performance & Load Testing (`locust/`)

This directory contains the [Locust](https://locust.io/) load-testing harness and PDF certificate harvester used to benchmark the KeySupport Validation Service (`rest-service`).

## Reference Instance Sizing (`https://keysupport.net`)

All benchmarks below were measured against the public `keysupport.net` reference deployment:

| Resource | Specification |
| --- | --- |
| **CPU** | 11th Gen Intel Core i7-1165G7 @ 2.80 GHz (4 cores / 8 threads) |
| **Host Memory** | 30 GiB RAM |
| **Container Footprint** | ~334 MiB steady-state RAM (141 cached FPKI intermediates + validation cache) |
| **Network** | 1 Gbps full-duplex Ethernet (`1000Mb/s`), behind a TLS 1.3 / ML-KEM reverse proxy |
| **Runtime** | `eclipse-temurin:25-jre-ubi10-minimal` (`Temurin-25.0.5.1+2-LTS`), Spring Boot 4.1.2, Jetty 12, `bc-fips` 2.1.3 |
| **Concurrency & Cache** | Java 25 virtual threads (`spring.threads.virtual.enabled: true`), `ValidationCacheSingleton` (`success-ttl-seconds: 900`, `affirmatively-invalid-ttl-seconds: 86400`, `max-entries: 10000`) |

## Benchmark Results (100 Zero-Wait Users, 15s)

Workload (`locust/locustfile.py`, `FastHttpUser`, `VSS_WAIT_MIN=0`, `VSS_WAIT_MAX=0`) across 17 valid CA certificates, 4 valid end-entity (EE) certificates, 15 negative EE/OCSP certificates, plus `/vss/v2/policies` and `/vss/v2/intermediates`:

| Metric | Uncached Baseline | Warm Cache + Virtual Threads | Improvement |
| --- | ---: | ---: | --- |
| **Aggregated Throughput** | 592.60 req/s | **1,047.39 req/s** | **+76.7%** |
| **Total Requests (15s)** | 8,711 | **15,386** | +76.6% |
| **Error Rate** | 0.00% | **0.00%** | — |
| **`POST /vss/v2/validate (SUCCESS)` p50** | 140 ms | **52 ms** | **2.7x faster** |
| **`POST /vss/v2/validate (SUCCESS)` p95** | 830 ms | **84 ms** | **9.9x faster** |
| **`POST /vss/v2/validate (SUCCESS)` p99** | 1,100 ms | **110 ms** | **10.0x faster** |
| **Overall Max Response Time** | 1,509 ms | **857 ms** | **Under 1 second** |

> **Cold-cache note:** On an initially cold cache at 100 zero-wait users (`1,041.94 req/s`, `0.00%` failures), initial live OCSP/CRL fetches produce a `1,689 ms` p99.99 tail while warming; once warm, all 15,386 requests complete in **≤ 857 ms**.

## Running the Load Test

Using the official Locust container image (no local Python venv required):

```bash
docker run --rm --network host \
  -e VSS_WAIT_MIN=0 -e VSS_WAIT_MAX=0 \
  -v "$PWD:/mnt/repo" -w /mnt/repo \
  locustio/locust:2.47.0 \
  -f locust/locustfile.py \
  --host https://keysupport.net \
  --headless -u 100 -r 50 -t 15s --only-summary
```

## Harvesting Additional Test Certificates

[`harvest_pdf_certs.py`](./harvest_pdf_certs.py) extracts CMS/PKCS#7 X.509 certificates from signed PDFs (for example, public `.gov` FPKI documents) and optionally classifies them against a VSS instance into `locust/corpus/valid/` and `locust/corpus/invalid/` (gitignored):

```bash
python3 locust/harvest_pdf_certs.py \
  --vss https://keysupport.net \
  --out locust/corpus \
  https://www.idmanagement.gov/docs/fpki-fpkima-audit-letter.pdf
```
