# Performance Testing

- See (Example Locust Test)[./agent.py]

## Basic Performance Test

- See (Example baseline Locust test against api.keysupport.org)[(./api-keysupport-org-example-from-local)]

```
$ locust -f agent.py 
[2022-12-03 18:42:17,355] Vostro/INFO/locust.main: Starting web interface at http://0.0.0.0:8089 (accepting connections from all network interfaces)
[2022-12-03 18:42:17,362] Vostro/INFO/locust.main: Starting Locust 2.13.1.dev24
[2022-12-03 18:43:10,720] Vostro/INFO/locust.runners: Ramping to 100 users at a rate of 10.00 per second
[2022-12-03 18:43:19,724] Vostro/INFO/locust.runners: All users spawned: {"FastUser": 100} (100 total users)
KeyboardInterrupt
2022-12-03T23:55:13Z
[2022-12-03 18:55:13,433] Vostro/INFO/locust.main: Shutting down (exit code 0)
Type     Name                                                                          # reqs      # fails |    Avg     Min     Max    Med |   req/s  failures/s
--------|----------------------------------------------------------------------------|-------|-------------|-------|-------|-------|-------|--------|-----------
POST     /vss/v2/validate                                                              548584     0(0.00%) |    107      30    4581     99 |  910.08        0.00
--------|----------------------------------------------------------------------------|-------|-------------|-------|-------|-------|-------|--------|-----------
         Aggregated                                                                    548584     0(0.00%) |    107      30    4581     99 |  910.08        0.00

Response time percentiles (approximated)
Type     Name                                                                                  50%    66%    75%    80%    90%    95%    98%    99%  99.9% 99.99%   100% # reqs
--------|--------------------------------------------------------------------------------|--------|------|------|------|------|------|------|------|------|------|------|------
POST     /vss/v2/validate                                                                       99    110    120    120    150    170    230    400    740   1100   4600 548584
--------|--------------------------------------------------------------------------------|--------|------|------|------|------|------|------|------|------|------|------|------
         Aggregated                                                                             99    110    120    120    150    170    230    400    740   1100   4600 548584
```

