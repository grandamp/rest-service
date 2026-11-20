# rest-service (Validation Service / VSS)

[![Build Pipeline](https://github.com/grandamp/rest-service/actions/workflows/build.yml/badge.svg)](https://github.com/grandamp/rest-service/actions/workflows/build.yml)
[![CodeQL Analysis with Temurin 25 and Maven](https://github.com/grandamp/rest-service/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/grandamp/rest-service/actions/workflows/codeql-analysis.yml)
[![SBOM](https://img.shields.io/badge/SBOM-CycloneDX-blue)](#software-bill-of-materials-sbom)

A modern, policy-driven RESTful **Validation Service (VSS)** for X.509 certificate path discovery, revocation checking, and policy validation—originally built to replace the legacy [Treasury SCVP service](https://github.com/GSA/ficam-scvp-testing/blob/master/utilities/vss2/README.md).

## Why Use VSS?

Many mTLS and identity-proofing architectures require complex X.509 path building, policy-tree evaluation, and CRL/OCSP revocation checking in the application layer—which is hard to configure consistently and hard to scale.

VSS centralizes certificate validation behind a lightweight JSON/REST API:

- **Policy-driven PKIX validation:** Define Trust Anchors, acceptable certificate policy OIDs, and RFC 5280 flags once in [`configuration/policies.json`](./configuration/policies.json).
- **Pre-validated intermediate cache:** Loads, validates, and periodically re-validates FPKI (or custom) intermediate CA bundles into an in-memory `CertStore`, eliminating per-request path-discovery bottlenecks.
- **FIPS & PQC-ready transport:** Built on Java 25, Spring Boot 4.1, BouncyCastle FIPS (`bc-fips`), and Jetty 12 configured for **TLS 1.3** with ML-KEM hybrid key exchange groups.
- **Full auditability:** Structured JSON logging of every validation request and response for post-authentication analysis.

## Live Reference Instance & API Docs

- **Swagger UI:** [https://keysupport.net/swagger-ui/index.html](https://keysupport.net/swagger-ui/index.html)
- **Open Issues:** [github.com/grandamp/rest-service/issues](https://github.com/grandamp/rest-service/issues)

## API Endpoints

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/vss/v2/validate` | Validate a Base64-encoded X.509 certificate against a `validationPolicyId` |
| `GET` | `/vss/v2/policies` | List active validation policies, Trust Anchors, and policy OID sets |
| `GET` | `/vss/v2/intermediates` | List currently cached intermediate CA certificates |
| `POST` | `/vss/v1/validate` | Legacy SCVP-compatible validation endpoint |

## Quick Start

### 1. Run with Docker (GHCR)

Prebuilt multi-layer container images are published on every `main` build to `ghcr.io/grandamp/rest-service:latest` (based on `eclipse-temurin:25-jre-ubi10-minimal`):

```bash
docker run -d \
  --name rest-service \
  --restart unless-stopped \
  --network host \
  -v /opt/vss/ext:/opt/vss/ext:z \
  ghcr.io/grandamp/rest-service:latest
```

Place optional overrides in `/opt/vss/ext/conf/vss.yml` and TLS PEM files in `/opt/vss/ext/tls/`.

### 2. Build & Test from Source

Requires **Java 25** and **Maven 3.9+**:

```bash
mvn clean test
mvn clean package spring-boot:repackage
```

See [TESTING.md](./TESTING.md) for local execution scripts and [locust/agent.py](./locust/agent.py) for load-testing instrumentation.

### 3. Included Clients

- **Python CLI & helpers** ([`client/python/`](./client/python)):
  - `validate.py` — validate one or more PEM/DER certificates against a policy
  - `policies.py` — inspect active validation policies
  - `intermediates.py` — inspect cached intermediate CAs
- **Java CLI** ([`client/java/`](./client/java) / `org.keysupport.api.client.RestClient`): standalone `java.net.http` client with SHA-256 `x5t#S256` verification.

## Configuration

Defaults live in [`src/main/resources/application.yml`](./src/main/resources/application.yml) and can be overridden externally via `/opt/vss/ext/conf/vss.yml` or Spring Boot environment variables:

- `service.policies.uri` — URL to the JSON policy definitions (defaults to [`configuration/policies.json`](./configuration/policies.json)).
- `service.intermediates.uri` — URL to the CMS/PKCS#7 certs-only intermediate CA bundle (defaults to the FPKI Federal Common Policy G2 bundle).
- `service.intermediates.validate-on-load`, `revocation-enabled`, `revalidate-interval-seconds` — control startup admission and periodic re-validation of cached intermediates.
- `service.validation.pkix.*` — control `aia-chase`, `allowed-aia-locations`, `max-path-length`, `revocation-enabled`, `revocation-ee-only`, `ocsp-enabled`, `crl-enabled`, and `crl-max-size` (default `52428800` bytes / 50 MiB).

## Example Use Case: mTLS Client Certificate Validation

After authenticating a user via mTLS (preferably using [RFC 9151 (CNSA)](https://www.rfc-editor.org/rfc/rfc9151) requirements), the mTLS relying party can query VSS to determine whether the certificate bound to the client's private key satisfies an organization's assurance policy.

Consider the [U.S. Federal PKI](https://playbooks.idmanagement.gov/fpki/) and [NIST SP 800-63B](https://pages.nist.gov/800-63-3/sp800-63b.html#43-authenticator-assurance-level-3). To verify whether an mTLS client is using an AAL3 credential, a suitable validation policy from an [RFC 5280](https://www.rfc-editor.org/rfc/rfc5280#section-6.1.1) perspective is:

```text
(a)  a prospective certification path of length n == {all FPKI Intermediates that have a relationship with FCPCAG2}
(b)  the current date/time
(c)  user-initial-policy-set ==
       {
            2.16.840.1.101.3.2.1.3.7,
            2.16.840.1.101.3.2.1.3.13,
            2.16.840.1.101.3.2.1.3.16,
            2.16.840.1.101.3.2.1.3.18,
            2.16.840.1.101.3.2.1.3.41
       }
(d)  trust anchor information == {FCPCAG2}
(e)  initial-policy-mapping-inhibit == false
(f)  initial-explicit-policy == true
(g)  initial-any-policy-inhibit == true
(h)  initial-permitted-subtrees == null
(i)  initial-excluded-subtrees == null
```

While [DoD publishes approved interoperability policy OIDs](https://dl.dod.cyber.mil/wp-content/uploads/pki-pke/txt/unclass-pki_interop_assurance_levels.txt), it does not directly map each OID to NIST 800-63 IAL/AAL. VSS expresses that mapping as a `validationPolicy` definition:

```json
{
  "validationPolicyId": "2.16.840.1.101.10.2.18.2.1.4",
  "validationPolicyName": "aal3",
  "validationPolicyDescription": "Derived from legacy LOA4 validation policy (2.16.840.1.101.10.2.18.2.1.4)",
  "trustAnchors": [
    {
      "x5t#S256": "X5rswkYWshkTcmAN2A9t0yDIyloM638JyYXr8GlpNPw=",
      "x509Certificate": "MIIF3TCCA8WgAwIBAgIUIeW5oMyVbeJ4ygErqP3Fipiz++owDQYJKoZIhvcNAQEMBQAwXDELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDENMAsGA1UECxMERlBLSTEkMCIGA1UEAxMbRmVkZXJhbCBDb21tb24gUG9saWN5IENBIEcyMB4XDTIwMTAxNDEzMzUxMloXDTQwMTAxNDEzMzUxMlowXDELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDENMAsGA1UECxMERlBLSTEkMCIGA1UEAxMbRmVkZXJhbCBDb21tb24gUG9saWN5IENBIEcyMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA19fTFzEmIRgQKkFty6+99sRRjCTYBYh7LloRpCZs4rgpBk+/5P4aZYd5v01GYBfOKywGJyFh4xk33/Q4yACoOT1uZOloNq/qhhT0r92UogKf77n5JgMhvg/bThVB3lxxahZQMM0YqUhg1rtaKRKsXm0AplhalNT6c3mA3YDSt4+75i105oE3JbsFjDY5DtGMYB9JIhxobtWTSnhL5E5HzO0GVI9UvhWAPVAhxm8oT4wxSOIjZ/MywXflfBrDktZu1PNsJkkYJpvFgDmSFuEPzivcOrytoPiPfgXMqY/P7zO4opLrh2EV5yA4XYEdoyA2dVD8jmm+Lk7zgRFah/84P2guxNtWpZAtQ9Nsag4w4EmtRq82JLqZQlyrMbvLvhWFecEkyfDzwGkFRIOBn1IbUfKTtN5GWpndl8HCUPbR2i7hpV9CFfkXTgsLGTwMNV2xPz2xThrLDu0jrDG+3/k42jB7KH3SQse72yo6MyNF46uumO7vORHlhOTVkWyxotBU327XZfq3BNupUDL6+R4dUG+pQADSstRJ60gePp0IAtQSHZYd1iRiXKpTLl0kofB2Y3LgAFNdYmaHrbrid0dlKIs9QioDwjm+wrDLAmuT4bjLZePhc3qt8ubjhZN2Naz+4YP5+nfSPPClLiyM/UT2el7eY4l6OaqXMIRfJxNIHwcCAwEAAaOBljCBkzAPBgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBBjAdBgNVHQ4EFgQU9CdcqcN8R/T6pqewWZeq3TUmF+MwUQYIKwYBBQUHAQsERTBDMEEGCCsGAQUFBzAFhjVodHRwOi8vcmVwby5mcGtpLmdvdi9mY3BjYS9jYUNlcnRzSXNzdWVkQnlmY3BjYWcyLnA3YzANBgkqhkiG9w0BAQwFAAOCAgEAAWQ3MAzwzr3O1RSBkg06NCj7eIL7/I5fwTBLhpoMhE0XoaoPUie0gqRo3KO2MhuBtacjy55ihIY87hShGoKQcbA1fh7e4Cly5QkOY+KbQsltkKzgod2zmPyC0bEOYD2LO141HyeDWdQ6dDXDz6dr8ObntOfMzgdo7vodCMuKU8+ysTdxRxTCi6AVz3uqe5k+ObJYpC0aXHNMy1OnFgL6oxMeGMlSecU/QUAIf0ncDurYFSctFwXitTC0CrcLO9/AGHqTFSHzUrIlbrgd/aGO+E3o3QoU+ThCPPnu1K2KZLG4pyMqdBm4y7rVGPRikLmFhIv/b6b2CL8yiYL0+mJDcrTVs0PYfALtQxMpSA8n053gajlPwhG3O5jcL8SzqlaGPmGqpnEi9aWAYHJXTzbjzGUAc2u8+Kw8Xv4JffhVWIxVKH4NS5PCtgXwxifgrmPi0/uU1w0crclEsSsya7FIBVRTURoSwwda25wIIWPIkQsQK1snJxgEyUzXi10MUDR0WSDqQAdhbOLcmcyhED5hphYQnf8sD8FpoUDjoLCPkU/ytfZoplmcBM4SQ4Ejgjyk63vMqBDcCMXTHciFTsV2e+aReLvIvU4YmaBQQl3vCFj1qMPIkRsTby1Ff8hRDQG3kH0vefcVtcicsdU8kV2Mee/xJ/c0cIHZWMw0HoRZPbo=",
      "X509SubjectName": "CN=Federal Common Policy CA G2, OU=FPKI, O=U.S. Government, C=US"
    }
  ],
  "userPolicySet": [
    "2.16.840.1.101.3.2.1.3.7",
    "2.16.840.1.101.3.2.1.3.13",
    "2.16.840.1.101.3.2.1.3.16",
    "2.16.840.1.101.3.2.1.3.18",
    "2.16.840.1.101.3.2.1.3.41"
  ],
  "inhibitPolicyMapping": false,
  "requireExplicitPolicy": true,
  "inhibitAnyPolicy": true
}
```

To validate a client certificate against this policy, send a `POST /vss/v2/validate` request (example using `curl` and [`jq`](https://jqlang.org/)):

```bash
curl -s -X 'POST' \
  'https://keysupport.net/vss/v2/validate' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
    "validationPolicyId": "2.16.840.1.101.10.2.18.2.1.4",
    "x509Certificate": "MIIFYjCCBEqgAwIBAgIRAOgIWMWQtieIECed/Q2JONIwDQYJKoZIhvcNAQELBQAwRjELMAkGA1UEBhMCVVMxIjAgBgNVBAoTGUdvb2dsZSBUcnVzdCBTZXJ2aWNlcyBMTEMxEzARBgNVBAMTCkdUUyBDQSAxRDQwHhcNMjIxMDEwMjAwMDU2WhcNMjMwMTA4MjAwMDU1WjAZMRcwFQYDVQQDEw5rZXlzdXBwb3J0Lm9yZzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALftackigK3dmFReOqr4txPVJrdTPRMbsz3+4ueBGtBd2OIKcFXDi8apCf76Fh8WBheDVOyDzUhkag+mTnPw0BXqYNyFntPcwosDkv7kviJgF4KmSWf3myTYmHoc0CAqoRqYsfTFkB7fK83OBnevqSZ8pgc2X7h6qSgx9Z/0dprl+2mWndIB82SZSCzpuaGXs5SPFDZVrB8Y7fUWqu1nPaTLfOzIlAIf8SHf1+f81D0Mg1rm/rAAi9HFwx+fLPnFvZUUWeErPEi9JmBJzU+TJdF3dDRldtJVgFdQXcvMMwgSjmlL+ToOwf/VRU/usIVGxLI/qoxLj+Y97ht5tzMILXMCAwEAAaOCAnYwggJyMA4GA1UdDwEB/wQEAwIFoDATBgNVHSUEDDAKBggrBgEFBQcDATAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBQHEqvLbOftuY4bf2Ojy3InQEZctzAfBgNVHSMEGDAWgBQl4hgOsleRlCrl1F2GkIPeU7O4kjB4BggrBgEFBQcBAQRsMGowNQYIKwYBBQUHMAGGKWh0dHA6Ly9vY3NwLnBraS5nb29nL3MvZ3RzMWQ0L1NRMUZ6enViUFlNMDEGCCsGAQUFBzAChiVodHRwOi8vcGtpLmdvb2cvcmVwby9jZXJ0cy9ndHMxZDQuZGVyMBkGA1UdEQQSMBCCDmtleXN1cHBvcnQub3JnMCEGA1UdIAQaMBgwCAYGZ4EMAQIBMAwGCisGAQQB1nkCBQMwPAYDVR0fBDUwMzAxoC+gLYYraHR0cDovL2NybHMucGtpLmdvb2cvZ3RzMWQ0L0oyajNSQ2lFeTA0LmNybDCCAQUGCisGAQQB1nkCBAIEgfYEgfMA8QB3AHoyjFTYty22IOo44FIe6YQWcDIThU070ivBOlejUutSAAABg8PAFU0AAAQDAEgwRgIhAInKXSrciK8OHtFWvQienDeIwBGCfDojh62EeoD62ngyAiEA6HO5HRCE00qCIKNCx6CuS9gDkvgrNm4xIS+gPc5Z8i8AdgCt9776fP8QyIudPZwePhhqtGcpXc+xDCTKhYY069yCigAAAYPDwBVHAAAEAwBHMEUCIQDOGktHy094r8OdqDhS7IPj44qJ1V9RRdR2ZEq4Sz5WOwIgcAlszeHnUNnp/jlDJPk1vIBJxh8/lokFwDBFiUU1EhAwDQYJKoZIhvcNAQELBQADggEBAGiaDoyFu4aLlLvsYnIVlTvf6bfUZ0xf3F9gSyjJBxFxFMF4BTiWWbCXGCpBT7+hHLSvq2fKKzKn2bR17XnDJkcVNBA5kxc9MQu6YE26+QkV4nk23hNidmaRimuiq4FLivsp/9gDrU+q5lutXP6n7EYI0ibXtuGk+Tx/qW9xvT4V9snhhw+4ks0Yw0B3AcdwNqRgmKYOftOLDPivLe5+lTmxbiMIPXSvkXzGVDFwj8/D8APp173Jg6XmHPHC9DSYF33e0q3nmmKQOV3PjR4mlFkY5Ewv9pKLJRnbwp01CArMs2YYdSqERZnLCH69lcHM/kuEKgMmdgLnEky2Xa03c84="
  }' | jq
```

*(In this example, validation fails because a public TLS server certificate does not validate under the Federal PKI `aal3` policy.)*

## Post-Authentication Certificate Analytics

VSS logs *all* validation requests and responses in structured JSON, enabling relying parties to run post-processing on certificate data to:

- Perform [GCD analysis of RSA keys](https://factorable.net/resources.html)
- Assess [PQC migration readiness of ECC key usage](https://www.whitehouse.gov/wp-content/uploads/2022/11/M-23-02-M-Memo-on-Migrating-to-Post-Quantum-Cryptography.pdf)
- [Lint certificates](https://github.com/GSA/fpkilint) to detect non-compliant issuers
- Improve data quality across a certificate issuance ecosystem

## Software Bill of Materials (SBOM)

Every Maven build generates a CycloneDX JSON SBOM embedded inside `rest-service-eb.jar` at `META-INF/sbom/application.cdx.json`, and uploads `application.cdx.json` as the `rest-service-sbom` GitHub Actions artifact.

To inspect the embedded SBOM from a packaged JAR:

```bash
unzip -p target/rest-service-eb.jar META-INF/sbom/application.cdx.json | jq
```

Builds also set `project.build.outputTimestamp` so identical inputs produce deterministic JAR SHA-256 digests.

## How to Contribute

Pull requests and issues are welcome at [github.com/grandamp/rest-service](https://github.com/grandamp/rest-service).

### Public Domain

This project is in the worldwide [public domain](LICENSE.md).

> This project is in the public domain within the United States, and copyright and related rights in the work worldwide are waived through the [CC0 1.0 Universal public domain dedication](https://creativecommons.org/publicdomain/zero/1.0/).
>
> All contributions to this project will be released under the CC0 dedication. By submitting a pull request, you are agreeing to comply with this waiver of copyright interest.
