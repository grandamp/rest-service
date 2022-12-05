# Docker

NOTE:  The [Dockerfile](./Dockerfile) may need to be altered to point to your local memcached server:

```
export MEMCACHED_CNF=server.lan
```

- Build

```
sudo docker build --tag rest-service-coretto-17 - < Dockerfile
```

- Run

```
sudo docker run --publish 5000:5000 rest-service-coretto-17
```