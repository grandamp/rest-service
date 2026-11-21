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
| **Runtime** | `eclipse-temurin:25-jre-ubi10-minimal` (`Temurin-25.0.5.1+2-LTS`), Spring Boot 4.1.2, Jetty 12, `bc-fips` 2.1.3, `-XX:+UseZGC -XX:MaxRAMPercentage=75.0` |
| **Concurrency & Cache** | Java 25 virtual threads (`spring.threads.virtual.enabled: true`), `ValidationCacheSingleton` (`success-ttl-seconds: 900`, `affirmatively-invalid-ttl-seconds: 86400`, `max-entries: 10000`), Async Logback (`CONSOLE` + `FILE`) |

## Benchmark Results (`locustio/locust:2.47.0`, Zero-Wait, 15s)

Workload (`locust/locustfile.py`, `FastHttpUser`, `VSS_WAIT_MIN=0`, `VSS_WAIT_MAX=0`) across 17 valid CA certificates, 4 valid end-entity (EE) certificates, 15 negative EE/OCSP certificates, plus `/vss/v2/policies` and `/vss/v2/intermediates`:

### Before vs. After Optimization (100 Users)

| Metric | Uncached Baseline | Cache + Virtual Threads | + Async Logback, Pre-Parse Cache, Controller Cache & ZGC |
| --- | ---: | ---: | ---: |
| **Aggregated Throughput** | 592.60 req/s | 1,047.39 req/s | **1,147.21 req/s** (**+93.6%**) |
| **Total Requests (15s)** | 8,711 | 15,386 | **16,926** |
| **Error Rate** | 0.00% | 0.00% | **0.00%** |
| **`POST /vss/v2/validate (SUCCESS)` p50** | 140 ms | 52 ms | **50 ms** |
| **`POST /vss/v2/validate (SUCCESS)` p95** | 830 ms | 84 ms | **80 ms** |
| **`POST /vss/v2/validate (SUCCESS)` p99** | 1,100 ms | 110 ms | **110 ms** |
| **Overall Max Response Time** | 1,509 ms | 857 ms | **830 ms** |

### Saturation Sweep (Max QPS with Max Response Time ≤ 1 Second)

| Concurrency (0 wait, 15s) | Aggregated QPS | # Reqs | # Fails | Aggregated p50 / p95 / p99 | Overall Max |
| --- | ---: | ---: | ---: | --- | ---: |
| **100 users (peak ≤ 1s)** | **1,147.21 req/s** | 16,926 | 0 (0.00%) | 51 / 86 / 130 ms | **830 ms** |
| **120 users** | 1,012.03 req/s | 14,971 | 0 (0.00%) | 65 / 110 / 170 ms | 1,721 ms |
| **150 users** | 939.19 req/s | 13,881 | 0 (0.00%) | 78 / 120 / 660 ms | 1,425 ms |

- **Peak throughput under the ≤ 1 second max latency ceiling:** **1,147.21 req/s** at **100 concurrent zero-wait users** (`830 ms` max, `0.00%` failures). Beyond 100 concurrent users on this 4c/8t host, TLS reverse-proxy and CPU contention reduce throughput and push tail latency above 1 second.
- **Cold-cache note:** On an initially cold cache at 100 zero-wait users (`1,041.94 req/s`, `0.00%` failures), initial live OCSP/CRL fetches produce a `1,689 ms` p99.99 tail while warming; once warm, all requests stay under **830 ms**.

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
