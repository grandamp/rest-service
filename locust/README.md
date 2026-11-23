# Performance & Load Testing (`locust/`)

This directory contains the [Locust](https://locust.io/) load-testing harness and three certificate-harvesting pipelines used to benchmark the KeySupport Validation Service (`rest-service`).

## Reference Instance Sizing (`https://keysupport.net`)

All benchmarks below were measured against the public `keysupport.net` reference deployment:

| Resource | Specification |
| --- | --- |
| **CPU** | 11th Gen Intel Core i7-1165G7 @ 2.80 GHz (4 cores / 8 threads) |
| **Host Memory** | 30 GiB RAM |
| **Container Footprint** | ~334 MiB steady-state RAM (141 cached FPKI intermediates + validation cache) |
| **Network & Proxy** | 1 Gbps full-duplex Ethernet (`1000Mb/s`), behind a TLS 1.3 / ML-KEM reverse proxy (`worker_processes auto;`, `worker_connections 4096;`, HTTP/1.1 `keepalive 128` upstream pool) |
| **Runtime** | `eclipse-temurin:25-jre-ubi10-minimal` (`Temurin-25.0.5.1+2-LTS`), Spring Boot 4.1.2, Jetty 12, `bc-fips` 2.1.3, `-XX:+UseZGC -XX:MaxRAMPercentage=75.0` |
| **Concurrency & Cache** | Java 25 virtual threads (`spring.threads.virtual.enabled: true`), `ValidationCacheSingleton` (`success-ttl-seconds: 900`, `affirmatively-invalid-ttl-seconds: 86400`, `max-entries: 10000`, `compact-audit-logging: true`), single-flight miss coalescing, Async Logback (`%logger{63}`, `includeCallerData=false`) |

## Benchmark Results (`locustio/locust:2.47.0`)

Workload (`locust/locustfile.py`, `FastHttpUser`) across 17 valid CA certificates, 4 valid end-entity (EE) certificates, 15 negative EE/OCSP certificates, plus `/vss/v2/policies` and `/vss/v2/intermediates`.

### 1. Stage-by-Stage Progression Since `v2.0` (100 Zero-Wait Users, 15s)

| Stage | Key Changes | QPS | Error Rate | `validate (SUCCESS)` p50 / p95 / p99 | Overall Max | Meets ≤ 1s Ceiling? |
| --- | --- | ---: | ---: | --- | ---: | --- |
| **`v2.0` Baseline** (`97724fc`) | Uncached `CertPathBuilder` + live OCSP/CRL on every request; platform threads; sync logging | 592.60 req/s | 0.00% | 140 / 830 / 1,100 ms | 1,509 ms | No |
| **+ Cache & Virtual Threads** (`PR #1018`) | In-memory `ValidationCacheSingleton` + Java 25 virtual threads | 1,047.39 req/s | 0.00% | 52 / 84 / 110 ms | 857 ms | Yes (**+76.7%**) |
| **+ Policy-Aware TTLs, Async Logback, Pre-Parse Cache, Controller Cache & ZGC** (`PR #1019`, `PR #1021`) | 15m `Success` / 24h `isAffirmativelyInvalid` TTLs; `AsyncAppender`; pre-parse `x5t#S256(byte[])` lookup; `IntermediatesController` `CertStore` cache; `-XX:+UseZGC` | **1,147.21 req/s** | 0.00% | **50 / 80 / 110 ms** | **830 ms** | Yes (**+93.6%**) |
| **+ No-Stack-Walk Logging, Pre-Serialized JSON, Single-Flight & Proxy Keepalive** (`PR #1023`, `PR #1024`, `08f7589`) | Removed `:%L` / `includeCallerData=false`; pre-serialized JSON in `CacheEntry`; `compact-audit-logging`; single-flight miss coalescing; proxy `worker_processes auto` + `keepalive 128` | 1,016.18 req/s | 0.00% | 52 / 94 / 140 ms | 846 ms | Yes (**+71.5%**) |

### 2. Paced Baseline (50 Users, `wait_time` 0.1–0.5s, 30s)

| Metric | `v2.0` Uncached (`97724fc`) | Current Optimized Stack (`08f7589`) | Improvement |
| --- | ---: | ---: | --- |
| **Aggregated Throughput** | 119.17 req/s (3,534 reqs) | **152.55 req/s** (4,516 reqs) | **+28.0%** |
| **Error Rate** | 0.00% | **0.00%** | — |
| **`POST /vss/v2/validate (SUCCESS)` p50** | 170 ms | **3 ms** | **56.7x faster** |
| **`POST /vss/v2/validate (SUCCESS)` p95** | 190 ms | **8 ms** | **23.8x faster** |
| **`POST /vss/v2/validate (SUCCESS)` p99** | 370 ms | **38 ms** | **9.7x faster** |
| **Overall Max Response Time** | 650 ms | **187 ms** | **3.5x lower** |

### 3. Full-Throughput Saturation Sweep (Zero-Wait, 15s Runs on `08f7589`)

| Concurrency (`VSS_WAIT_MIN=0`, `VSS_WAIT_MAX=0`) | Aggregated QPS | # Reqs | # Fails | Aggregated p50 / p95 / p99 | Overall Max |
| --- | ---: | ---: | ---: | --- | ---: |
| **100 users (peak ≤ 1s)** | **1,016.18 req/s** | 14,992 | 0 (0.00%) | 52 / 94 / 140 ms | **846 ms** |
| **120 users** | 992.45 req/s | 14,638 | 0 (0.00%) | 61 / 110 / 180 ms | 1,638 ms |
| **150 users** | 971.94 req/s | 14,359 | 0 (0.00%) | 74 / 120 / 620 ms | 2,228 ms |
| **200 users** | 932.29 req/s | 13,817 | 0 (0.00%) | 93 / 140 / 1,600 ms | 2,850 ms |

- **Peak throughput under the ≤ 1 second max latency ceiling:** **~1,016–1,147 req/s** at **100 concurrent zero-wait users** (`830–846 ms` max, `0.00%` failures; uncached `v2.0` baseline at 100 users was `592.60 req/s` with `1,509 ms` max).
- **20-user warm-up (5s):** `827.92 req/s`, `12 ms` p50, `35 ms` p95, `523 ms` max, `0.00%` failures.
- **Cold-cache note:** On an initially cold cache at 100 zero-wait users (`1,041.94 req/s`, `0.00%` failures), initial live OCSP/CRL fetches produce a `1,689 ms` p99.99 tail while warming; once warm, all requests stay under **830–846 ms**.
- **Bottleneck beyond 100 zero-wait users:** On this single 4-core/8-thread 1 Gbps host, TLS 1.3 + ML-KEM proxy encryption, 1 Gbps NIC bandwidth (especially `GET /vss/v2/intermediates` payloads), and single-process Locust client contention cap throughput around ~1,000–1,150 req/s and push tail latency above 1 second at 120+ concurrent zero-wait clients.

## Running the Load Test

Using the official Locust container image (no local Python venv required):

```bash
# Paced baseline (50 users, 30s)
docker run --rm --network host \
  -v "$PWD:/mnt/repo" -w /mnt/repo \
  locustio/locust:2.47.0 \
  -f locust/locustfile.py \
  --host https://keysupport.net \
  --headless -u 50 -r 10 -t 30s --only-summary

# Full-throughput zero-wait test (100 users, 15s)
docker run --rm --network host \
  -e VSS_WAIT_MIN=0 -e VSS_WAIT_MAX=0 \
  -v "$PWD:/mnt/repo" -w /mnt/repo \
  locustio/locust:2.47.0 \
  -f locust/locustfile.py \
  --host https://keysupport.net \
  --headless -u 100 -r 50 -t 15s --only-summary
```

## Harvesting Additional Test Certificates

`locust/locustfile.py` automatically loads any `.pem` certificates placed under `locust/corpus/valid/` and `locust/corpus/invalid/` (both gitignored). Three complementary harvesting use cases are provided under `locust/` to build and refresh that corpus. Generated `.txt` URL lists under `locust/` (other than `requirements.txt`) and `locust/corpus/` are gitignored.

---

### Use Case 1: Mining Publicly Available Signed PDFs (`locust/public-pdf-mining/`)

Discover public signed PDFs (for example, `.gov` FPKI/PIV-signed documents), extract their CMS/PKCS#7 X.509 certificates, and classify them against a VSS instance:

| Script | Purpose |
| --- | --- |
| [`public-pdf-mining/crawl_federal_pdfs.py`](./public-pdf-mining/crawl_federal_pdfs.py) | Zero-API-key crawler that extracts direct `.pdf` links from federal index pages and XML sitemaps, deduplicating against existing URL lists. |
| [`public-pdf-mining/batch_harvest_pdf_certs.py`](./public-pdf-mining/batch_harvest_pdf_certs.py) | Bulk downloader/extractor that reads PDF paths or URLs from `-f <file>` / stdin (`-`) / positional args, retries transient errors, extracts CMS/PKCS#7 certs, and classifies them via VSS. |
| [`public-pdf-mining/harvest_pdf_certs.py`](./public-pdf-mining/harvest_pdf_certs.py) | Lightweight single/multi-input extractor for local PDF files or direct URLs. |

All three scripts support `--ca-bundle <path>` (a PEM file or directory of `*.pem`/`*.crt`/`*.cer` CAs) and `-k` / `--insecure` for legacy or PKI-gated hosts.

```bash
# 1. Crawl default federal deviation/CMA indexes (depth 1) and deduplicate
python3 locust/public-pdf-mining/crawl_federal_pdfs.py \
  --depth 1 \
  --out locust/urls_crawled.txt

# 2. Crawl a specific index using a custom CA bundle or -k
python3 locust/public-pdf-mining/crawl_federal_pdfs.py \
  --seed https://www.acq.osd.mil/dpap/dars/class_deviations.html \
  --depth 1 \
  -k \
  --exclude-from locust/urls_crawled.txt \
  --out locust/mil_crawled.txt

# 3. Crawl an XML sitemap with a regex path filter
python3 locust/public-pdf-mining/crawl_federal_pdfs.py \
  --sitemap https://nasa.gov \
  --match "/saa/" \
  --out locust/nasa_saa.txt

# 4. Batch-harvest from a URL list file and classify into locust/corpus/{valid,invalid}
python3 locust/public-pdf-mining/batch_harvest_pdf_certs.py \
  --out locust/corpus \
  --vss https://keysupport.net \
  -f locust/urls_crawled.txt

# 5. Or harvest a single PDF directly
python3 locust/public-pdf-mining/harvest_pdf_certs.py \
  --vss https://keysupport.net \
  --out locust/corpus \
  https://www.idmanagement.gov/docs/fpki-fpkima-audit-letter.pdf
```

---

### Use Case 2: Mining Observed Certificates from VSS Service Logs (`locust/vss-log-mining/`)

[`vss-log-mining/mine_vss_logs.py`](./vss-log-mining/mine_vss_logs.py) streams local or remote (`--ssh`) VSS JSON/text service logs (including rotated `.log` and `.gz` files), extracts every unique Base64 DER X.509 certificate observed in v1 (`/vss/rest`) and v2 (`/vss/v2/validate`) requests/responses, deduplicates by SHA-256 fingerprint, and files them into `locust/corpus/{valid,invalid,unclassified}/`.

```bash
# Mine local log directory and classify live against VSS
python3 locust/vss-log-mining/mine_vss_logs.py \
  /path/to/vss/logs \
  --vss https://keysupport.net \
  --out locust/corpus

# Stream matching certificate lines over SSH from a remote deployment (no full log download)
python3 locust/vss-log-mining/mine_vss_logs.py \
  --ssh user@vss-host:/opt/vss/ext/logs \
  --ssh-identity ~/.ssh/id_rsa \
  --vss https://keysupport.net \
  --out locust/corpus

# Offline mode: classify using the validationResult already recorded in the logs
python3 locust/vss-log-mining/mine_vss_logs.py \
  /path/to/vss/logs \
  --use-log-result \
  --out locust/corpus
```

Useful flags:
- `--include-ca`: include CA certificates in addition to end-entity/OCSP leaf certificates (CAs are skipped by default).
- `--reclassify`: revalidate and move/overwrite certificates already present in `locust/corpus/`.
- `--file-glob "vss*.log*"`: customize log filename matching inside local or remote directories.
- `--ca-bundle <path>` / `-k`: custom TLS trust or insecure verification when calling `--vss`.

---

### Use Case 3: Mining Historical FPKI Intermediate Bundles (`locust/fpki-intermediate-mine/`)

[`fpki-intermediate-mine/mine_fpki_intermediates.py`](./fpki-intermediate-mine/mine_fpki_intermediates.py) retrieves the last `N` GitHub commits (default `50`) of `GSA/idmanagement.gov`'s `CACertificatesValidatingToFederalCommonPolicyG2.p7b` bundle, extracts every unique CA/intermediate certificate across those revisions (including expired, revoked, and decommissioned CAs), and classifies them into `locust/corpus/{valid,invalid,unclassified}/`.

```bash
# Mine the last 50 bundle revisions and classify live against VSS
python3 locust/fpki-intermediate-mine/mine_fpki_intermediates.py \
  --vss https://keysupport.net \
  --out locust/corpus

# Mine a custom revision count or target .p7b file without live classification
python3 locust/fpki-intermediate-mine/mine_fpki_intermediates.py \
  --versions 50 \
  --out locust/corpus
```

Useful flags:
- `--versions <N>`: number of recent GitHub commits to scan (default: `50`).
- `--repo`, `--branch`, `--path`: override the GitHub repository, branch, or `.p7b` path.
- `--reclassify`: revalidate and move/overwrite certificates already present in `locust/corpus/`.
- `--token`: optional GitHub API token (defaults to `GITHUB_TOKEN` / `GH_TOKEN`).

---

### Verifying & Cleaning the Corpus (`locust/revalidate_corpus.py`)

Because previously valid certificates can expire or be revoked over time (causing `locustfile.py` assertions to fail), [`revalidate_corpus.py`](./revalidate_corpus.py) re-validates every file under `locust/corpus/{valid,invalid,unclassified}/` against a known-good VSS instance at the current time:

- Moves newly expired or revoked certificates from `valid/` → `invalid/` (and promotes newly valid certificates from `invalid/` or `unclassified/` → `valid/`).
- Canonicalizes filenames to `<sha256[:16]>.pem` and removes duplicate or unparseable files.

```bash
# Preview any corpus drift or cleanup actions without modifying files
python3 locust/revalidate_corpus.py --vss https://keysupport.net --dry-run

# Revalidate, move expired/revoked certs, deduplicate, and clean locust/corpus/
python3 locust/revalidate_corpus.py --vss https://keysupport.net --corpus locust/corpus

# Strict check mode (exits non-zero if any drift or corrupt/duplicate files exist)
python3 locust/revalidate_corpus.py --vss https://keysupport.net --check
```
