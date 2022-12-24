
# rest-service

[![Build Status](https://github.com/grandamp/rest-service/actions/workflows/build.yml/badge.svg)](https://github.com/grandamp/rest-service/actions)

[![OpenAPI v3 Compliance](https://validator.swagger.io/validator?url=https://api.keysupport.org/v3/api-docs)](https://api.keysupport.org/v3/api-docs)

A WIP to replace the [Treasury SCVP service](https://github.com/GSA/ficam-scvp-testing/blob/master/utilities/vss2/README.md), with something more modern.

Why?  Many mTLS use-cases involve complex certificate validation in the application layer, which is *hard* to scale.

This implementation is intended to allow a relying party/team to centralize that validation using a simple API, easily hosted by the relying party/team locally, or via [AWS Elastic Beanstalk](/AWS-EBS.md).

Here is some [basic performance information](./locust/README.md).

## Example use/business-case (mTLS Client Certificate Validation)

After authenticating a user via mTLS [(preferably v1.3)](https://www.rfc-editor.org/rfc/rfc8446), a relying party can request validation from the service to determine if the certificate that is bound to the private key meets a given organization/team derived validation policy.

Consider the [U.S. Federal PKI](https://playbooks.idmanagement.gov/fpki/), and; [NIST SP 800-63-3](https://pages.nist.gov/800-63-3/sp800-63b.html#43-authenticator-assurance-level-3).  If you would like to determine if a given mTLS user is leveraging IAL3/AAL3 credentials, a suitable validation policy [from an RFC 5280 perspective](https://www.rfc-editor.org/rfc/rfc5280#section-6.1.1) *may* be:

```text
(a)  a prospective certification path of length n == {all FPKI Intermediates that have a relationship with FCPCAG2}
(b)  the current date/time
(c)  user-initial-policy-set ==
       {
            2.16.840.1.101.3.2.1.3.7,
            2.16.840.1.101.3.2.1.3.13,
            2.16.840.1.101.3.2.1.3.15,
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

This service can implement such a policy using the service's following `validationPolicy` definition:

```text
{
    "validationPolicyId": "cc54e0ec-49da-333a-8150-2dd00b758b17",
    "validationPolicyName": "aal3",
    "validationPolicyDescription": "Derived from legacy LOA4 validation policy (2.16.840.1.101.10.2.18.2.1.4)",
    "trustAnchors": [{
        "trustAnchorId": "5F9AECC24616B2191372600DD80F6DD320C8CA5A0CEB7F09C985EBF0696934FC",
        "trustAnchorSubject": "CN=Federal Common Policy CA G2, OU=FPKI, O=U.S. Government, C=US",
        "x509Certificate": "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"
    }],
    "userPolicySet": [
        "2.16.840.1.101.3.2.1.3.7",
        "2.16.840.1.101.3.2.1.3.13",
        "2.16.840.1.101.3.2.1.3.15",
        "2.16.840.1.101.3.2.1.3.16",
        "2.16.840.1.101.3.2.1.3.18",
        "2.16.840.1.101.3.2.1.3.41"
    ],
    "inhibitPolicyMapping": false,
    "requireExplicitPolicy": true,
    "inhibitAnyPolicy": true
}
```

To validate a certificate that was presented by the client against this policy, the client simply needs to send the following request to the service [(such as my example service)](https://api.keysupport.org/swagger-ui/index.html), and parse the response [{example via `curl` and `jq`}](https://stedolan.github.io/jq/):

```text
curl -v -X 'POST' \
'https://api.keysupport.org/vss/v2/validate' \
-H 'accept: application/json' \
-H 'Content-Type: application/json' \
-d '{
"validationPolicyId": "c21f969b-5f03-333d-83e0-4f8f136e7682",
"x509Certificate": "MIIFYjCCBEqgAwIBAgIRAOgIWMWQtieIECed/Q2JONIwDQYJKoZIhvcNAQELBQAwRjELMAkGA1UEBhMCVVMxIjAgBgNVBAoTGUdvb2dsZSBUcnVzdCBTZXJ2aWNlcyBMTEMxEzARBgNVBAMTCkdUUyBDQSAxRDQwHhcNMjIxMDEwMjAwMDU2WhcNMjMwMTA4MjAwMDU1WjAZMRcwFQYDVQQDEw5rZXlzdXBwb3J0Lm9yZzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALftackigK3dmFReOqr4txPVJrdTPRMbsz3+4ueBGtBd2OIKcFXDi8apCf76Fh8WBheDVOyDzUhkag+mTnPw0BXqYNyFntPcwosDkv7kviJgF4KmSWf3myTYmHoc0CAqoRqYsfTFkB7fK83OBnevqSZ8pgc2X7h6qSgx9Z/0dprl+2mWndIB82SZSCzpuaGXs5SPFDZVrB8Y7fUWqu1nPaTLfOzIlAIf8SHf1+f81D0Mg1rm/rAAi9HFwx+fLPnFvZUUWeErPEi9JmBJzU+TJdF3dDRldtJVgFdQXcvMMwgSjmlL+ToOwf/VRU/usIVGxLI/qoxLj+Y97ht5tzMILXMCAwEAAaOCAnYwggJyMA4GA1UdDwEB/wQEAwIFoDATBgNVHSUEDDAKBggrBgEFBQcDATAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBQHEqvLbOftuY4bf2Ojy3InQEZctzAfBgNVHSMEGDAWgBQl4hgOsleRlCrl1F2GkIPeU7O4kjB4BggrBgEFBQcBAQRsMGowNQYIKwYBBQUHMAGGKWh0dHA6Ly9vY3NwLnBraS5nb29nL3MvZ3RzMWQ0L1NRMUZ6enViUFlNMDEGCCsGAQUFBzAChiVodHRwOi8vcGtpLmdvb2cvcmVwby9jZXJ0cy9ndHMxZDQuZGVyMBkGA1UdEQQSMBCCDmtleXN1cHBvcnQub3JnMCEGA1UdIAQaMBgwCAYGZ4EMAQIBMAwGCisGAQQB1nkCBQMwPAYDVR0fBDUwMzAxoC+gLYYraHR0cDovL2NybHMucGtpLmdvb2cvZ3RzMWQ0L0oyajNSQ2lFeTA0LmNybDCCAQUGCisGAQQB1nkCBAIEgfYEgfMA8QB3AHoyjFTYty22IOo44FIe6YQWcDIThU070ivBOlejUutSAAABg8PAFU0AAAQDAEgwRgIhAInKXSrciK8OHtFWvQienDeIwBGCfDojh62EeoD62ngyAiEA6HO5HRCE00qCIKNCx6CuS9gDkvgrNm4xIS+gPc5Z8i8AdgCt9776fP8QyIudPZwePhhqtGcpXc+xDCTKhYY069yCigAAAYPDwBVHAAAEAwBHMEUCIQDOGktHy094r8OdqDhS7IPj44qJ1V9RRdR2ZEq4Sz5WOwIgcAlszeHnUNnp/jlDJPk1vIBJxh8/lokFwDBFiUU1EhAwDQYJKoZIhvcNAQELBQADggEBAGiaDoyFu4aLlLvsYnIVlTvf6bfUZ0xf3F9gSyjJBxFxFMF4BTiWWbCXGCpBT7+hHLSvq2fKKzKn2bR17XnDJkcVNBA5kxc9MQu6YE26+QkV4nk23hNidmaRimuiq4FLivsp/9gDrU+q5lutXP6n7EYI0ibXtuGk+Tx/qW9xvT4V9snhhw+4ks0Yw0B3AcdwNqRgmKYOftOLDPivLe5+lTmxbiMIPXSvkXzGVDFwj8/D8APp173Jg6XmHPHC9DSYF33e0q3nmmKQOV3PjR4mlFkY5Ewv9pKLJRnbwp01CArMs2YYdSqERZnLCH69lcHM/kuEKgMmdgLnEky2Xa03c84="
}' | jq
```

With the example above, the validation would fail, because a TLS client using my API gateway certificate would not validate successfully via the policy described above.

The service logs *all* requests, as well as *all* responses.  This allows for post authentication analysis of the certificates to improve the overall infrastructure.

As a relying party, you can perform post-processing on the logs for:

- [GCD Analysis of RSA keys based on modulus size](https://factorable.net/resources.html)
- [Determine PQC risk of ECC key usage](https://www.whitehouse.gov/wp-content/uploads/2022/11/M-23-02-M-Memo-on-Migrating-to-Post-Quantum-Cryptography.pdf)
- [Lint all certificates](https://github.com/GSA/fpkilint) to find non-compliant issuers
- Improve data quality within a certificate issuance ecosystem
- ...

## Building and Testing

- See [Testing](/TESTING.md)

- Advanced Test Case, see [mTLS Testing (please open an issue if this link fails, inclusive of a screenshot)](https://x509.keysupport.org/swagger-ui/index.html)

## Deploying to AWS Elasitic Beanstalk

- See [AWS Elastic Beanstalk](/AWS-EBS.md)

## Current API Documentation & API Example

- See/Try the operational example via [this Swagger-UI](https://api.keysupport.org/swagger-ui/index.html), based on the above example [AWS Elastic Beanstalk](/AWS-EBS.md) deployment
- [Current issues](https://github.com/grandamp/rest-service/issues) apply to example above

## How to Contribute

The source repository exists [here](https://github.com/grandamp/rest-service).

### Public domain

This project is in the worldwide [public domain](LICENSE.md).

> This project is in the public domain within the United States, and copyright and related rights in the work worldwide are waived through the [CC0 1.0 Universal public domain dedication](https://creativecommons.org/publicdomain/zero/1.0/).
>
> All contributions to this project will be released under the CC0 dedication. By submitting a pull request, you are agreeing to comply with this waiver of copyright interest.
