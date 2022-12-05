# Performance Testing

- See [Example Locust Test](./agent.py)

## Basic Performance Test (AWS ElasticBeanstalk Hosted Service [2 - t2.micro instances])

- See [Example baseline Locust test against api.keysupport.org](./api-keysupport-org-example-from-local/total_requests_per_second_1670110955.png)

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

## Basic Performance Test ([Locally hosted Docker image](../docker/README.md) on a local server named `server.lan`)

- See [Example baseline Locust test against server.lan](./api-keysupport-org-example-from-local/total_requests_per_second_1670110955.png)

Note/TODO: Test accuracy is questionable due to the Warnings below. Should scale test with more workers via a test *not via my laptop*

```
$ locust -f agent.py 
[2022-12-04 19:51:53,465] Vostro/INFO/locust.main: Starting web interface at http://0.0.0.0:8089 (accepting connections from all network interfaces)
[2022-12-04 19:51:53,473] Vostro/INFO/locust.main: Starting Locust 2.13.1.dev24
[2022-12-04 19:52:50,392] Vostro/INFO/locust.runners: Ramping to 100 users at a rate of 1.00 per second
[2022-12-04 19:54:23,529] Vostro/WARNING/root: CPU usage above 90%! This may constrain your throughput and may even give inconsistent response time measurements! See https://docs.locust.io/en/stable/running-distributed.html for how to distribute the load over multiple CPU cores or machines
[2022-12-04 19:54:29,545] Vostro/INFO/locust.runners: All users spawned: {"FastUser": 100} (100 total users)
[2022-12-04 20:02:51,636] Vostro/WARNING/locust.runners: CPU usage was too high at some point during the test! See https://docs.locust.io/en/stable/running-distributed.html for how to distribute the load over multiple CPU cores or machines
KeyboardInterrupt
2022-12-05T01:03:54Z
[2022-12-04 20:03:54,487] Vostro/INFO/locust.main: Shutting down (exit code 0)
Type     Name                                                                          # reqs      # fails |    Avg     Min     Max    Med |   req/s  failures/s
--------|----------------------------------------------------------------------------|-------|-------------|-------|-------|-------|-------|--------|-----------
POST     /vss/v2/validate                                                             1527015     0(0.00%) |     26       2    5199     24 | 2539.77        0.00
--------|----------------------------------------------------------------------------|-------|-------------|-------|-------|-------|-------|--------|-----------
         Aggregated                                                                   1527015     0(0.00%) |     26       2    5199     24 | 2539.77        0.00

Response time percentiles (approximated)
Type     Name                                                                                  50%    66%    75%    80%    90%    95%    98%    99%  99.9% 99.99%   100% # reqs
--------|--------------------------------------------------------------------------------|--------|------|------|------|------|------|------|------|------|------|------|------
POST     /vss/v2/validate                                                                       24     28     30     32     37     44     57     74    170   2300   5200 1527015
--------|--------------------------------------------------------------------------------|--------|------|------|------|------|------|------|------|------|------|------|------
         Aggregated                                                                             24     28     30     32     37     44     57     74    170   2300   5200 1527015
```
