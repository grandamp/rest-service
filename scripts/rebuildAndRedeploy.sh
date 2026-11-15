#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.." || exit 1
/usr/bin/git pull
/usr/bin/mvn clean package spring-boot:repackage
# Kill and prune docker image
/usr/bin/docker build -t vss .
/usr/bin/docker run -d --restart unless-stopped --network host -v /opt/vss/ext:/opt/vss/ext vss:latest
