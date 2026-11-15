#!/bin/bash
/usr/bin/docker system prune --all --volumes -f
/usr/bin/mvn clean package spring-boot:repackage
/usr/bin/docker build -t vss .
/usr/bin/docker run -d --restart unless-stopped --network host -v /opt/vss/ext:/opt/vss/ext vss:latest
