# Python Client Example

This is a quick and dirty Python client example.

When using on the command line, you should be able to parse the following types of files to validate the enclosed certificates:

- .p7c
-- May be 1 DER encoded CMS file containing multiple certificates
- .p7b
-- May be 1 DER encoded CMS file containing multiple certificates
- .p7s
-- May be 1 DER encoded CMS file containing multiple certificates
- .p7m
-- May be 1 DER encoded CMS file containing multiple certificates
- .pem
-- May be 1..N PEM encoded certificates
- .cer
-- May be 1 DER encoded certificate
- .crt
-- May be 1 DER encoded certificate
- .der
-- May be 1 DER encoded certificate

Example Usage:

```TEXT
/usr/bin/curl -v https://raw.githubusercontent.com/GSA/ficam-playbooks/federalist-pages/_fpki/tools/CACertificatesValidatingToFederalCommonPolicyG2.p7b --output /tmp/CACertificatesValidatingToFederalCommonPolicyG2.p7b

./vssPythonClientExample.py /tmp/CACertificatesValidatingToFederalCommonPolicyG2.p7b https://api.keysupport.org/vss/v2/validate c21f969b-5f03-333d-83e0-4f8f136e7682

```

Example output:

```TEXT
```
