# Testing

## Manual Testing via localhost:8080

At this point in time, the testing is manual, and functional.  I.e., does the service properly validate the certificate in contrast to the current production validation services.

I've been running the Spring Boot service on localhost:5000, so when I make a change, I build and start the service, then execute the `curl` commands below.  

The service is currently using S3 for fetching the current policies, which can be overridden using the example locally via [policies.json](./configuration/policies.json).  

Using the following to execute and test:

- Execute

`./local_execute.sh`

- Test (Via another terminal)

`./current_local_tests.sh`

## Manual Testing via Elastic Beanstalk [Swagger-UI](https://api.keysupport.org/swagger-ui/index.html)

I deploy the updated .war to a basic AWS Elastic Beanstalk environment, then execute the `curl` commands below.

- Elastic Beanstalk Deployment

$ `eb status api-keysupport-rest-dev`
```
Environment details for: api-keysupport-rest-dev
  Application name: api-keysupport-rest
  Region: us-east-1
  Deployed Version: app-221102_153606522127
  Environment ID: e-kytfpa4r53
  Platform: arn:aws:elasticbeanstalk:us-east-1::platform/Corretto 11 running on 64bit Amazon Linux 2/3.4.0
  Tier: WebServer-Standard-1.0
  CNAME: api-keysupport-rest-dev.us-east-1.elasticbeanstalk.com
  Updated: 2022-11-02 19:42:10.042000+00:00
  Status: Ready
  Health: Green
```
  
$ `eb deploy api-keysupport-rest-dev`
```
Uploading: [##################################################] 100% Done...
2022-11-03 16:04:14    INFO    Environment update is starting.      
2022-11-03 16:04:20    INFO    Deploying new version to instance(s).
2022-11-03 16:04:22    INFO    Batch 1: Starting application deployment on instance(s) [i-05546c224e6feb165].
2022-11-03 16:04:23    INFO    Batch 1: De-registering instance(s) from the load balancer and waiting for them to go out of service.
2022-11-03 16:04:36    INFO    Batch 1: Starting application deployment command execution.
2022-11-03 16:04:38    INFO    Instance deployment successfully detected a JAR file in your source bundle.
2022-11-03 16:04:38    INFO    Instance deployment successfully generated a 'Procfile'.
2022-11-03 16:04:41    INFO    Instance deployment completed successfully.
2022-11-03 16:04:45    INFO    Batch 1: Completed application deployment command execution.
2022-11-03 16:04:45    INFO    Command execution completed on 1 of 2 instances in environment.
2022-11-03 16:04:45    INFO    Batch 1: Registering instance(s) with the load balancer and waiting for them to be healthy. 
2022-11-03 16:07:11    INFO    Batch 1: Completed application deployment.
2022-11-03 16:07:11    INFO    Batch 2: Starting application deployment on instance(s) [i-0bf65cf5730f466e7].
2022-11-03 16:07:12    INFO    Batch 2: De-registering instance(s) from the load balancer and waiting for them to go out of service.
2022-11-03 16:07:25    INFO    Batch 2: Starting application deployment command execution.
2022-11-03 16:07:27    INFO    Instance deployment successfully detected a JAR file in your source bundle.
2022-11-03 16:07:27    INFO    Instance deployment successfully generated a 'Procfile'.
2022-11-03 16:07:30    INFO    Instance deployment completed successfully.
2022-11-03 16:07:35    INFO    Batch 2: Completed application deployment command execution.
2022-11-03 16:07:35    INFO    Command execution completed on 2 of 2 instances in environment.
2022-11-03 16:07:35    INFO    Batch 2: Registering instance(s) with the load balancer and waiting for them to be healthy. 
2022-11-03 16:10:00    INFO    Batch 2: Completed application deployment.
2022-11-03 16:10:05    INFO    New application version was deployed to running EC2 instances.
2022-11-03 16:10:05    INFO    Environment update completed successfully.
```
                                                                      
$ `eb status api-keysupport-rest-dev`
```
  Environment details for: api-keysupport-rest-dev
  Application name: api-keysupport-rest
  Region: us-east-1
  Deployed Version: app-fac7-221103_120410954843
  Environment ID: e-kytfpa4r53
  Platform: arn:aws:elasticbeanstalk:us-east-1::platform/Corretto 11 running on 64bit Amazon Linux 2/3.4.0
  Tier: WebServer-Standard-1.0
  CNAME: api-keysupport-rest-dev.us-east-1.elasticbeanstalk.com
  Updated: 2022-11-03 16:10:05.703000+00:00
  Status: Ready
  Health: Green
```

- State TAPTest (revoked)

`/usr/bin/curl -v --header "Content-Type: application/json" --request POST --data '{"validationPolicyId": "c21f969b-5f03-333d-83e0-4f8f136e7682","x509Certificate":"MIIKNzCCCB+gAwIBAgIEYIWQKTANBgkqhkiG9w0BAQsFADCBuzETMBEGCgmSJomT8ixkARkWA3NidTEVMBMGCgmSJomT8ixkARkWBXN0YXRlMRYwFAYDVQQDDA1Db25maWd1cmF0aW9uMREwDwYDVQQDDAhTZXJ2aWNlczEcMBoGA1UEAwwTUHVibGljIEtleSBTZXJ2aWNlczEMMAoGA1UEAwwDQUlBMTYwNAYDVQQDDC1VLlMuIERlcGFydG1lbnQgb2YgU3RhdGUgQUQgSGlnaCBBc3N1cmFuY2UgQ0EwHhcNMjIwNjI5MTIzNjA3WhcNMjUwNjI5MTMwNjA3WjCBljETMBEGCgmSJomT8ixkARkWA3NidTEVMBMGCgmSJomT8ixkARkWBXN0YXRlMRswGQYKCZImiZPyLGQBGRYLYXBwc2VydmljZXMxHDAaBgNVBAsME0VudGVycHJpc2UgU2VydmljZXMxDDAKBgNVBAsMA1BLSTEMMAoGA1UECwwDVEFQMREwDwYDVQQDDAhUQVBUZXN0NDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALScG/lyri4gpn3ZEX1KCmulT7zpjGjhZtB+2nzd6LM3ulj5PavgKE7Y2kOs5mge691a6RaW89u/xubvHxDL/FEzd2jxvfjJST64bejhVjqIc1zt/E1YZlv1ClK927wWqmq4PsyP63FV1UyEOO9HvrUZ5AFI/n1HMJm+zuYVXiPCgUMOmqAkceN5GznoN9AVbNgkz2tI/3PqYCtfpy6CYs+LCpXyx7/Wcf65W3PJy6xr6hVUBSKUCryl76A5xS+eM5cvF1C0BfSPBtQBbpdbGJTKs3oh4DhLNfFbBPG6Es6LicHAHpQywiCE6X0ia+btzIe4CJoOPn1NGHwtGViBJzsCAwEAAaOCBWQwggVgMA4GA1UdDwEB/wQEAwIGwDApBgNVHSUEIjAgBggrBgEFBQcDAgYKKwYBBAGCNxQCAgYIKwYBBQUHAwQwQQYDVR0gBDowODAMBgpghkgBZQMCAQYBMAwGCmCGSAFlAwIBBgIwDAYKYIZIAWUDAgEGAzAMBgpghkgBZQMCAQYEMIICMgYIKwYBBQUHAQEEggIkMIICIDBEBggrBgEFBQcwAoY4aHR0cDovL2NybHMucGtpLnN0YXRlLmdvdi9BSUEvQ2VydHNJc3N1ZWRUb0RvU0FESEFDQS5wN2MwgcgGCCsGAQUFBzAChoG7bGRhcDovL2Rpci5wa2kuc3RhdGUuZ292L2NuPVUuUy4lMjBEZXBhcnRtZW50JTIwb2YlMjBTdGF0ZSUyMEFEJTIwSGlnaCUyMEFzc3VyYW5jZSUyMENBLGNuPUFJQSxjbj1QdWJsaWMlMjBLZXklMjBTZXJ2aWNlcyxjbj1TZXJ2aWNlcyxjbj1Db25maWd1cmF0aW9uLGRjPXN0YXRlLGRjPXNidT9jQUNlcnRpZmljYXRlO2JpbmFyeTCBzwYIKwYBBQUHMAKGgcJsZGFwOi8vZGlyLnBraS5zdGF0ZS5nb3YvY249VS5TLiUyMERlcGFydG1lbnQlMjBvZiUyMFN0YXRlJTIwQUQlMjBIaWdoJTIwQXNzdXJhbmNlJTIwQ0EsY249QUlBLGNuPVB1YmxpYyUyMEtleSUyMFNlcnZpY2VzLGNuPVNlcnZpY2VzLGNuPUNvbmZpZ3VyYXRpb24sZGM9c3RhdGUsZGM9c2J1P2Nyb3NzQ2VydGlmaWNhdGVQYWlyO2JpbmFyeTA7BggrBgEFBQcwAYYvaHR0cDovL29jc3AucGtpLnN0YXRlLmdvdi9PQ1NQL0RvU09DU1BSZXNwb25kZXIwLQYDVR0RBCYwJKAiBgorBgEEAYI3FAIDoBQMElRhcFRlc3Q0QHN0YXRlLmdvdjCCAgEGA1UdHwSCAfgwggH0MIIBFaCCARGgggENhjFodHRwOi8vY3Jscy5wa2kuc3RhdGUuZ292L2NybHMvRG9TQURQS0lIQUNBLTEuY3JshoHXbGRhcDovL2Rpci5wa2kuc3RhdGUuZ292L2NuPVdpbkNvbWJpbmVkMSxjbj1VLlMuJTIwRGVwYXJ0bWVudCUyMG9mJTIwU3RhdGUlMjBBRCUyMEhpZ2glMjBBc3N1cmFuY2UlMjBDQSxjbj1BSUEsY249UHVibGljJTIwS2V5JTIwU2VydmljZXMsY249U2VydmljZXMsY249Q29uZmlndXJhdGlvbixkYz1zdGF0ZSxkYz1zYnU/Y2VydGlmaWNhdGVSZXZvY2F0aW9uTGlzdDtiaW5hcnkwgdiggdWggdKkgc8wgcwxEzARBgoJkiaJk/IsZAEZFgNzYnUxFTATBgoJkiaJk/IsZAEZFgVzdGF0ZTEWMBQGA1UEAwwNQ29uZmlndXJhdGlvbjERMA8GA1UEAwwIU2VydmljZXMxHDAaBgNVBAMME1B1YmxpYyBLZXkgU2VydmljZXMxDDAKBgNVBAMMA0FJQTE2MDQGA1UEAwwtVS5TLiBEZXBhcnRtZW50IG9mIFN0YXRlIEFEIEhpZ2ggQXNzdXJhbmNlIENBMQ8wDQYDVQQDDAZDUkw4NzQwKwYDVR0QBCQwIoAPMjAyMjA2MjkxMjM2MDdagQ8yMDI0MDgwNDE3MDYwN1owHwYDVR0jBBgwFoAUheMozyV6pgGNxW/+xVJyqp6jTE4wHQYDVR0OBBYEFJEQPx/gZOO50vXJRGxBPNbmm2XEMAkGA1UdEwQCMAAwDQYJKoZIhvcNAQELBQADggIBAFxgss5EJJ0dBpgN3Rh+F2o65DxUrRYSxK++wPQuJ/YJmyUf2ns0581nfAVLDutuL0MjDdZqeT2WkUNTvvVVcYrHvainF9ZTwHpZ0XNhAOJ6IbVGiz1bhQBAi9MSMenAgnXYZ18IFUq8Sg/EyutvDlKtydMPzqJXJW/f7+0r95HJnuvVZPhsB3YdHotxQ0GXAdQhRLKHWR2Grx/NcWtFG9mMNsMez0DeY8EuOqllx34gCvgHwQ/UP8Ng2oJBkRMMEVKCzCnQVHEirpV0PM8LRrjeLeu2+Zsx1Cwf/AhgcYQIhCLWJjXyBKeD8joVmyuWBrsP4JPrxgp/tSMDhfOuS+w3pllVHK53CyXWigxXdolEeePUpAh3jmlm0nK3vUIbz0hHJf1bwhJJAEl+F1QMiDTHfbB2xFHnYlN48AU6hK6g9eG49t05NcEbPCTCUjE+T6gi+qqvjXeZ+685fTHjgeBCaKSord0RgUhYrwHOYeErW8Oih4SB+5ryl5pvKAzwvgJ34LsTxcVK1QOZsIovTIH1pWEYBhd9YWWF6zevQKkxcnMRa+YOGIzPJwH/RMr86mEoU02JGCCoNw89raetcSNBt7ikDiJ5EWcrb7VmqXCEEwZl8mea7I7/NhktQQC5wqq9uArf/mU279MzfHSxH1VosCLWBXWxUcWhKW6eMxry"}' https://api.keysupport.org/vss/v2/validate | jq`

- Current Treasury Root CA Signed by FCPCAG2 (valid)

`/usr/bin/curl -v --header "Content-Type: application/json" --request POST --data '{"validationPolicyId": "c21f969b-5f03-333d-83e0-4f8f136e7682","x509Certificate":"MIIJLjCCBxagAwIBAgIUJ58Jc3/l3T11NL4OpRr/ncQBhQEwDQYJKoZIhvcNAQEMBQAwXDELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDENMAsGA1UECxMERlBLSTEkMCIGA1UEAxMbRmVkZXJhbCBDb21tb24gUG9saWN5IENBIEcyMB4XDTIyMDQwNjE3MDg0MFoXDTI1MDQwNjE3MDg0MFowgY4xCzAJBgNVBAYTAlVTMRgwFgYDVQQKEw9VLlMuIEdvdmVybm1lbnQxIzAhBgNVBAsTGkRlcGFydG1lbnQgb2YgdGhlIFRyZWFzdXJ5MSIwIAYDVQQLExlDZXJ0aWZpY2F0aW9uIEF1dGhvcml0aWVzMRwwGgYDVQQLExNVUyBUcmVhc3VyeSBSb290IENBMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA7D5nzQgGJWbAzFCMv5x7nb7bZ1ERbKGEfKVLg7XWT8xTsL8CaItldWtTGGwbjiTH+sbLmk19jkfCQ7QhyipMHDfmFxEAa/aTc28nWquT/Omt1yEunX2qQK7XA42gGYLRfkjcV8wr/gcHieQDERUKUSYPo/ecrzfcJ7S7xRpIKqiBPlD5msWJjBHBsgZWvMpvT2tZuOU3nK47oQ3FNZtHUiUkYUtQieMRwk8TQ8Y0fdZ+rwJxWTo44LUJp4hXPgtdSSe+DFDJv+le8Ncvzw1cH8lJ8sjPjFvFCjeWVZVFhDC/HR2BqnC7vqcSAyWCwsIaNNfn11kruLMf87SUdqKwWeLH+xJOh5slKV91+pee7HqUYIawO3bLCeHZ2TXQfoN37n224IeFgzpR2t4fVRLlYYeZuFxRb4vInCIFMwvlmorOXitVCfaZd71Ws9GKO3Sg3ur9sNvKgBeE7A4mm5bEVRBS0Gpo+s6L9jdUPYvrzV1bRx1f4IfIwuSbxl93Mn1JLLNFPS1nAHhROc1NzTf/1annVnPWt49xvJfeKmFagwkMKv3wFqa0UHF9TO8TYcO5jueOwfiHY6e9ASElT0ev5Wk3kaoP5wPWeP8Rhkt1HnD9puitgAiUNHsEol7osemoRQdlzmg5jZE306KGzwjbgNdX4QN8iGp/vt3rg+0sFVkCAwEAAaOCA7MwggOvMB8GA1UdIwQYMBaAFPQnXKnDfEf0+qansFmXqt01JhfjMB0GA1UdDgQWBBQXS7gmuml6rRJQV0Uxnle7dKXaLzAOBgNVHQ8BAf8EBAMCAQYwDwYDVR0TAQH/BAUwAwEB/zCB3QYDVR0gBIHVMIHSMAwGCmCGSAFlAwIBAwEwDAYKYIZIAWUDAgEDAjAMBgpghkgBZQMCAQMSMAwGCmCGSAFlAwIBAxMwDAYKYIZIAWUDAgEDFDAMBgpghkgBZQMCAQMGMAwGCmCGSAFlAwIBAwcwDAYKYIZIAWUDAgEDCDAMBgpghkgBZQMCAQMkMAwGCmCGSAFlAwIBAw0wDAYKYIZIAWUDAgEDEDAMBgpghkgBZQMCAQMRMAwGCmCGSAFlAwIBAycwDAYKYIZIAWUDAgEDKDAMBgpghkgBZQMCAQMpMIIBeQYDVR0hBIIBcDCCAWwwGAYKYIZIAWUDAgEDAQYKYIZIAWUDAgEFAjAYBgpghkgBZQMCAQMCBgpghkgBZQMCAQUDMBgGCmCGSAFlAwIBAwYGCmCGSAFlAwIBAwYwGAYKYIZIAWUDAgEDBgYKYIZIAWUDAgEFBzAYBgpghkgBZQMCAQMHBgpghkgBZQMCAQMHMBgGCmCGSAFlAwIBAwcGCmCGSAFlAwIBBQQwGAYKYIZIAWUDAgEDEAYKYIZIAWUDAgEDEDAYBgpghkgBZQMCAQMQBgpghkgBZQMCAQUFMBgGCmCGSAFlAwIBAxIGCmCGSAFlAwIBBQowGAYKYIZIAWUDAgEDEwYKYIZIAWUDAgEFCzAYBgpghkgBZQMCAQMUBgpghkgBZQMCAQUMMBgGCmCGSAFlAwIBAxIGCmCGSAFlAwIBAy0wGAYKYIZIAWUDAgEDEwYKYIZIAWUDAgEDLjAYBgpghkgBZQMCAQMUBgpghkgBZQMCAQMvMEAGCCsGAQUFBwELBDQwMjAwBggrBgEFBQcwBYYkaHR0cDovL3BraS50cmVhc3VyeS5nb3Yvcm9vdF9zaWEucDdjMBIGA1UdJAEB/wQIMAaAAQCBAQAwDQYDVR02AQH/BAMCAQAwUQYIKwYBBQUHAQEERTBDMEEGCCsGAQUFBzAChjVodHRwOi8vcmVwby5mcGtpLmdvdi9mY3BjYS9jYUNlcnRzSXNzdWVkVG9mY3BjYWcyLnA3YzA3BgNVHR8EMDAuMCygKqAohiZodHRwOi8vcmVwby5mcGtpLmdvdi9mY3BjYS9mY3BjYWcyLmNybDANBgkqhkiG9w0BAQwFAAOCAgEAcicSaU1ju+btaAOfCyP9Mx/sKibvR/mcEH6Ci8fHrham75+mR7fyQ7C6PZhCQhFO3z0jLxW6IzpnKhpzp0oqJOkV75WkqKoCd/awpWyPwAtrWHMjyb6s7AHcFwjAC0heK96ZMr+SOM7XopVYIAnQ4tYe1ON5lDBLmoJOpHOIz1E4E+ubcwTuWygLAyL5IUHGYJQLM6J/bDhbRDbz6aeCxShXWZP7Aa+jhi0N1ZmyHrZ1uukPpMX9R/qqhXSjzRYwxq6wozdbh+aj2OU3ZdRVKaCC04k9zr4lFVq1RtKc34iYqtpbBCm1IWaLH1Uo4aovvJlxwEPEI0XBa50ILCkEYeOCTk59kBWgTTNx9R7FFAA+DoTW1Y+1VibZpXxkgpBpFmiYBoI9LfwNh50n/lixxxoIGqe/fTup1yEabophqNchBlK5tRcfHDdAd24Vq4MCq1G+zUVzdLHK8nXcXGzNWa/KZvEsaAOkLx1bGyxp0D8bYmsKWummm/jlMYq1RGHxFRMXPMbcn+IZmw5t8bC7wITvRlToRl6CCfE1cSx69cgOqIVFIs43J18nymUYpKOirp3Km8uT47UyHTEgtn3VLKMhW1sN1zEjyYl4WcMoGlja2xW0Wy8TFld3a+0O1YyH3BZhuC/1MvSaRVE64mHWnwnZqnDYw4xJ1OS1Q+l18Eg="}' https://api.keysupport.org/vss/v2/validate | jq`

- My PIV Auth Certificate (Swagger `curl` example)

```
curl -X 'POST' \
  'https://api.keysupport.org/vss/v2/validate' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "validationPolicyId": "c21f969b-5f03-333d-83e0-4f8f136e7682",
  "x509Certificate": "MIIHtzCCBp+gAwIBAgIEYkH8IjANBgkqhkiG9w0BAQsFADCBgjELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDEjMCEGA1UECxMaRGVwYXJ0bWVudCBvZiB0aGUgVHJlYXN1cnkxIjAgBgNVBAsTGUNlcnRpZmljYXRpb24gQXV0aG9yaXRpZXMxEDAOBgNVBAsTB09DSU8gQ0EwHhcNMjIwODE1MTYyMzQxWhcNMjUwODE1MTY1MTM3WjCBrTELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDEjMCEGA1UECxMaRGVwYXJ0bWVudCBvZiB0aGUgVHJlYXN1cnkxJTAjBgNVBAsTHEJ1cmVhdSBvZiB0aGUgRmlzY2FsIFNlcnZpY2UxDzANBgNVBAsTBlBlb3BsZTEnMA0GA1UEBRMGNDAzNjExMBYGA1UEAxMPVG9kZCBFLiBKb2huc29uMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArB57JliuGCIvEmVb4Ro/UMZBxkw/9RpHx8K39rnhfndwgo3qTmZWToJAI3EDFADUxzOhkhoL44gbzub3MjmLWTrFU9UWHCGf3XlMfZxO0MibtE9U2hFPQ37VtxcTMsN+DQbxEka/P6jrvzfWrI1CDDs5mvfFCJu2Os+xQemwm95pqvncHT3bF4Z+uC3oDmz/LW2XFrDPjH2Cy5oRTst1X0wrvQwyDBkyFMbEqhXp+YBGLnXOO71P+9nxEUQyVeVbPu0gqT+cjhdIvPH7oppDjW6wQnh5nTkTZBjek0QHx6CoOJveQjTmxeqYEcwq9G+FJJBKLWWRB1kgJwVTkb8ngQIDAQABo4IEBjCCBAIwDgYDVR0PAQH/BAQDAgeAMBcGA1UdIAQQMA4wDAYKYIZIAWUDAgEDDTAyBgNVHSUEKzApBgorBgEEAYI3FAICBggrBgEFBQcDAgYHKwYBBQIDBAYIKwYBBQUHAxUwEAYJYIZIAWUDBgkBBAMBAQAwggEIBggrBgEFBQcBAQSB+zCB+DAwBggrBgEFBQcwAoYkaHR0cDovL3BraS50cmVhcy5nb3YvdG9jYV9lZV9haWEucDdjMIGgBggrBgEFBQcwAoaBk2xkYXA6Ly9sZGFwLnRyZWFzLmdvdi9vdT1PQ0lPJTIwQ0Esb3U9Q2VydGlmaWNhdGlvbiUyMEF1dGhvcml0aWVzLG91PURlcGFydG1lbnQlMjBvZiUyMHRoZSUyMFRyZWFzdXJ5LG89VS5TLiUyMEdvdmVybm1lbnQsYz1VUz9jQUNlcnRpZmljYXRlO2JpbmFyeTAhBggrBgEFBQcwAYYVaHR0cDovL29jc3AudHJlYXMuZ292MIG3BgNVHREEga8wgaygMAYKKwYBBAGCNxQCA6AiDCBUT0RELkpPSE5TT05ARklTQ0FMLlRSRUFTVVJZLkdPVoEgVG9kZC5Kb2huc29uQGZpc2NhbC50cmVhc3VyeS5nb3agJwYIYIZIAWUDBgagGwQZ0gJEWCELbNQ2AQ2haFoBCEOSEaSCAhDD64YtdXJuOnV1aWQ6MWEzOWY5MWQtYTgzYy00ZjRlLWFlYjMtMmM3YWVmZGE4ZGFiMIIBiQYDVR0fBIIBgDCCAXwwJ6AloCOGIWh0dHA6Ly9wa2kudHJlYXMuZ292L09DSU9fQ0E1LmNybDCCAU+gggFLoIIBR6SBlzCBlDELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDEjMCEGA1UECxMaRGVwYXJ0bWVudCBvZiB0aGUgVHJlYXN1cnkxIjAgBgNVBAsTGUNlcnRpZmljYXRpb24gQXV0aG9yaXRpZXMxEDAOBgNVBAsTB09DSU8gQ0ExEDAOBgNVBAMTB0NSTDMxMDKGgapsZGFwOi8vbGRhcC50cmVhcy5nb3YvY249Q1JMMzEwMixvdT1PQ0lPJTIwQ0Esb3U9Q2VydGlmaWNhdGlvbiUyMEF1dGhvcml0aWVzLG91PURlcGFydG1lbnQlMjBvZiUyMHRoZSUyMFRyZWFzdXJ5LG89VS5TLiUyMEdvdmVybm1lbnQsYz1VUz9jZXJ0aWZpY2F0ZVJldm9jYXRpb25MaXN0O2JpbmFyeTAfBgNVHSMEGDAWgBTNmhxgcsHrvq7Fq6xJkOtNjvHfrjAdBgNVHQ4EFgQUOr9oOidr/PJmV/STexf+AdOEgNwwDQYJKoZIhvcNAQELBQADggEBABowU1EHNoHIB0yHpmisSJS7NDMoKQp2BVeDkwSmPsf3GN3hMXIfaNsGaGpweEUT4wM3H/C83Z6NQZaJcWBm7s1jDQvGqBc+sU0YnKzPh61VCVENFDZT/tlwqS8DCEBA+Jk681mLdpWLhpkp5rrmkc2Hcl0RqtKX6zgBtj3WyRLqW/+zrqTXnBGl6yTbbmUxVmvck3xfXHe7M6ytBlzvTEOFzlzjkaDO/keYZDuutlaw/F9AqyBVOP6Jjx2JOyfx/EZTpgc4+rIarfRqJ+YVwRe0ULHh3+1BV0qr6apxd+02TsHgjwGSstO4jviJEBNxZZq2lalDWD8gVmDkhFX4UIQ="
}' | jq
```
