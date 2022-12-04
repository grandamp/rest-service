#!/usr/bin/python
 
import time
from locust import task
from locust.contrib.fasthttp import FastHttpUser
from time import gmtime, strftime
import random
 
import requests
requests.packages.urllib3.disable_warnings()

import resource

resource.setrlimit(resource.RLIMIT_NOFILE, (999999, 999999))
 
class FastUser(FastHttpUser):
 
#    @task
#    def status(self):
#        with self.client.get("/vss/v2/policies",verify=False, timeout=(30, 30), catch_response=True) as response:
#            if response.status_code == 0:
#                print("Empty Response")
#                response.failure(response)
#            elif response.status_code != 200:
#                print("Policy Query Failure!")
#                response.failure(response)
#            else:
#                response.success()
 
    @task
    def validateTreasuryHeavy(self):
        time = strftime("%Y-%m-%dT%H:%M:%SZ", gmtime())
        with self.client.post("/vss/v2/validate", data=None, json={"validationPolicyId": "c21f969b-5f03-333d-83e0-4f8f136e7682","x509Certificate":"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","clientMetadata":{"client":"Locust Test %s" % time}}, verify=False, timeout=(30, 30),
        catch_response=True) as response:
            if response.status_code == 0:
                print("Empty Response")
                response.failure(response)
            else:
                response.success()
 

    @task
    def validateTreasuryLight(self):
        time = strftime("%Y-%m-%dT%H:%M:%SZ", gmtime())
        with self.client.post("/vss/v2/validate", data=None, json={"validationPolicyId": "c21f969b-5f03-333d-83e0-4f8f136e7682","x509Certificate":"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","clientMetadata":{"client":"Locust Test %s" % time}}, verify=False, timeout=(30, 30),
        catch_response=True) as response:
            if response.status_code == 0:
                print("Empty Response")
                response.failure(response)
            else:
                response.success()

    @task
    def validateTreasuryLight(self):
        time = strftime("%Y-%m-%dT%H:%M:%SZ", gmtime())
        with self.client.post("/vss/v2/validate", data=None, json={"validationPolicyId": "c21f969b-5f03-333d-83e0-4f8f136e7682","x509Certificate": "MIIEuTCCA6GgAwIBAgICBUwwDQYJKoZIhvcNAQELBQAwWzELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDEMMAoGA1UECxMDRG9EMQwwCgYDVQQLEwNQS0kxFjAUBgNVBAMTDURvRCBSb290IENBIDMwHhcNMjEwNjAxMTQxMTIzWhcNMjcwNjAyMTQxMTIzWjBaMQswCQYDVQQGEwJVUzEYMBYGA1UEChMPVS5TLiBHb3Zlcm5tZW50MQwwCgYDVQQLEwNEb0QxDDAKBgNVBAsTA1BLSTEVMBMGA1UEAxMMRE9EIElEIENBLTY1MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnkK9OCQ+D0b/7SLsEs0LCElhKIzGtiZDBw9VLqCaxTHlxaYEPV/B/X9NGoP5PE4ToBOSramLCMPbwjadhNk8O0gEInZCuEzV17vvx6O4xg+FJ9OO76LU1KeXJnnvX1YnCKz3uxrn3sw1jQugEEd1yPwKoHMjJ2Sr7Vgrm1e983EgiRint9lble7x/MDLvEZDELeeqhPZvRiz1qwVG+/p2ks980qFLFLl1INOUSPnSLIbafg7cWE9yTC5i99s4pJnP2ThyBv6JsgFzbbj9FEYGyh75GjIMEv8ulcQ3ATOSBREUPzrd6sQmideeqvxXrDYxo8Qel6brZiti+5vEr3OzQIDAQABo4IBhjCCAYIwHwYDVR0jBBgwFoAUbIqUonexgHIdgXoWqvLczmbuRcAwHQYDVR0OBBYEFGLgSDhWbW9rJb67w4hYsaycQ8lbMA4GA1UdDwEB/wQEAwIBhjBnBgNVHSAEYDBeMAsGCWCGSAFlAgELJDALBglghkgBZQIBCycwCwYJYIZIAWUCAQsqMAsGCWCGSAFlAgELOzAMBgpghkgBZQMCAQMNMAwGCmCGSAFlAwIBAxEwDAYKYIZIAWUDAgEDJzASBgNVHRMBAf8ECDAGAQH/AgEAMAwGA1UdJAQFMAOAAQAwNwYDVR0fBDAwLjAsoCqgKIYmaHR0cDovL2NybC5kaXNhLm1pbC9jcmwvRE9EUk9PVENBMy5jcmwwbAYIKwYBBQUHAQEEYDBeMDoGCCsGAQUFBzAChi5odHRwOi8vY3JsLmRpc2EubWlsL2lzc3VlZHRvL0RPRFJPT1RDQTNfSVQucDdjMCAGCCsGAQUFBzABhhRodHRwOi8vb2NzcC5kaXNhLm1pbDANBgkqhkiG9w0BAQsFAAOCAQEAF8Uj33K0ZM9adtfd8IM2ebqwgbgRxi22Pb6bKkKOkGV2NU4wMckpuRpUrQGJmy6CIXZ84QWz9DZSNAU0nyHXB6PLbSV0jnzKygWO7IOv83M6dcnCG8QUP1o20V0NGhzNBEtKjxWENZCYHEruxm+2rB+MBngPhkBgdni2npetHX2e1cmsgMS6G1PUh2idhZ8Mpdofnr+V0GuKLpwiNz3hLnKehl2Bs6aHG2IIOm/PdzvsKCP2eiGzS3SiiCf6fukYoYBNedL8fHfFNyM4UPNgc4eG+bu0GJK4wKPVjiX7xYDdGaYZ2m4Y++zrKuMq+Oar6DQGq3SERMAZCDYsEt3z2g==","clientMetadata":{"client":"Locust Test %s" % time}}, verify=False, timeout=(30, 30),
        catch_response=True) as response:
            if response.status_code == 0:
                print("Empty Response")
                response.failure(response)
            else:
                response.success()

    @task
    def validateTreasuryLight(self):
        time = strftime("%Y-%m-%dT%H:%M:%SZ", gmtime())
        with self.client.post("/vss/v2/validate", data=None, json={"validationPolicyId": "c21f969b-5f03-333d-83e0-4f8f136e7682","x509Certificate": "MIIHtzCCBp+gAwIBAgIEYkH8IjANBgkqhkiG9w0BAQsFADCBgjELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDEjMCEGA1UECxMaRGVwYXJ0bWVudCBvZiB0aGUgVHJlYXN1cnkxIjAgBgNVBAsTGUNlcnRpZmljYXRpb24gQXV0aG9yaXRpZXMxEDAOBgNVBAsTB09DSU8gQ0EwHhcNMjIwODE1MTYyMzQxWhcNMjUwODE1MTY1MTM3WjCBrTELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDEjMCEGA1UECxMaRGVwYXJ0bWVudCBvZiB0aGUgVHJlYXN1cnkxJTAjBgNVBAsTHEJ1cmVhdSBvZiB0aGUgRmlzY2FsIFNlcnZpY2UxDzANBgNVBAsTBlBlb3BsZTEnMA0GA1UEBRMGNDAzNjExMBYGA1UEAxMPVG9kZCBFLiBKb2huc29uMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArB57JliuGCIvEmVb4Ro/UMZBxkw/9RpHx8K39rnhfndwgo3qTmZWToJAI3EDFADUxzOhkhoL44gbzub3MjmLWTrFU9UWHCGf3XlMfZxO0MibtE9U2hFPQ37VtxcTMsN+DQbxEka/P6jrvzfWrI1CDDs5mvfFCJu2Os+xQemwm95pqvncHT3bF4Z+uC3oDmz/LW2XFrDPjH2Cy5oRTst1X0wrvQwyDBkyFMbEqhXp+YBGLnXOO71P+9nxEUQyVeVbPu0gqT+cjhdIvPH7oppDjW6wQnh5nTkTZBjek0QHx6CoOJveQjTmxeqYEcwq9G+FJJBKLWWRB1kgJwVTkb8ngQIDAQABo4IEBjCCBAIwDgYDVR0PAQH/BAQDAgeAMBcGA1UdIAQQMA4wDAYKYIZIAWUDAgEDDTAyBgNVHSUEKzApBgorBgEEAYI3FAICBggrBgEFBQcDAgYHKwYBBQIDBAYIKwYBBQUHAxUwEAYJYIZIAWUDBgkBBAMBAQAwggEIBggrBgEFBQcBAQSB+zCB+DAwBggrBgEFBQcwAoYkaHR0cDovL3BraS50cmVhcy5nb3YvdG9jYV9lZV9haWEucDdjMIGgBggrBgEFBQcwAoaBk2xkYXA6Ly9sZGFwLnRyZWFzLmdvdi9vdT1PQ0lPJTIwQ0Esb3U9Q2VydGlmaWNhdGlvbiUyMEF1dGhvcml0aWVzLG91PURlcGFydG1lbnQlMjBvZiUyMHRoZSUyMFRyZWFzdXJ5LG89VS5TLiUyMEdvdmVybm1lbnQsYz1VUz9jQUNlcnRpZmljYXRlO2JpbmFyeTAhBggrBgEFBQcwAYYVaHR0cDovL29jc3AudHJlYXMuZ292MIG3BgNVHREEga8wgaygMAYKKwYBBAGCNxQCA6AiDCBUT0RELkpPSE5TT05ARklTQ0FMLlRSRUFTVVJZLkdPVoEgVG9kZC5Kb2huc29uQGZpc2NhbC50cmVhc3VyeS5nb3agJwYIYIZIAWUDBgagGwQZ0gJEWCELbNQ2AQ2haFoBCEOSEaSCAhDD64YtdXJuOnV1aWQ6MWEzOWY5MWQtYTgzYy00ZjRlLWFlYjMtMmM3YWVmZGE4ZGFiMIIBiQYDVR0fBIIBgDCCAXwwJ6AloCOGIWh0dHA6Ly9wa2kudHJlYXMuZ292L09DSU9fQ0E1LmNybDCCAU+gggFLoIIBR6SBlzCBlDELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDEjMCEGA1UECxMaRGVwYXJ0bWVudCBvZiB0aGUgVHJlYXN1cnkxIjAgBgNVBAsTGUNlcnRpZmljYXRpb24gQXV0aG9yaXRpZXMxEDAOBgNVBAsTB09DSU8gQ0ExEDAOBgNVBAMTB0NSTDMxMDKGgapsZGFwOi8vbGRhcC50cmVhcy5nb3YvY249Q1JMMzEwMixvdT1PQ0lPJTIwQ0Esb3U9Q2VydGlmaWNhdGlvbiUyMEF1dGhvcml0aWVzLG91PURlcGFydG1lbnQlMjBvZiUyMHRoZSUyMFRyZWFzdXJ5LG89VS5TLiUyMEdvdmVybm1lbnQsYz1VUz9jZXJ0aWZpY2F0ZVJldm9jYXRpb25MaXN0O2JpbmFyeTAfBgNVHSMEGDAWgBTNmhxgcsHrvq7Fq6xJkOtNjvHfrjAdBgNVHQ4EFgQUOr9oOidr/PJmV/STexf+AdOEgNwwDQYJKoZIhvcNAQELBQADggEBABowU1EHNoHIB0yHpmisSJS7NDMoKQp2BVeDkwSmPsf3GN3hMXIfaNsGaGpweEUT4wM3H/C83Z6NQZaJcWBm7s1jDQvGqBc+sU0YnKzPh61VCVENFDZT/tlwqS8DCEBA+Jk681mLdpWLhpkp5rrmkc2Hcl0RqtKX6zgBtj3WyRLqW/+zrqTXnBGl6yTbbmUxVmvck3xfXHe7M6ytBlzvTEOFzlzjkaDO/keYZDuutlaw/F9AqyBVOP6Jjx2JOyfx/EZTpgc4+rIarfRqJ+YVwRe0ULHh3+1BV0qr6apxd+02TsHgjwGSstO4jviJEBNxZZq2lalDWD8gVmDkhFX4UIQ=","clientMetadata":{"client":"Locust Test %s" % time}}, verify=False, timeout=(30, 30),
        catch_response=True) as response:
            if response.status_code == 0:
                print("Empty Response")
                response.failure(response)
            else:
                response.success()

