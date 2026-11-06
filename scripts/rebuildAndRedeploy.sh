#!/bin/bash
cd /home/tejohnson/eclipse-workspace/rest-service
/usr/bin/git pull
/usr/bin/mvn clean package spring-boot:repackage
# Kill and prune docker image
/usr/bin/docker build -t vss .
/usr/bin/docker run -d --restart unless-stopped --network host -v /opt/vss/ext:/opt/vss/ext vss:latest
