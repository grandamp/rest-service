#!/bin/bash
/usr/bin/docker pull ghcr.io/grandamp/rest-service:latest
# Kill and prune docker image
/usr/bin/docker run -d --restart unless-stopped --network host -e BASE_URI=https://keysupport.net -v /opt/vss/ext:/opt/vss/ext ghcr.io/grandamp/rest-service:latest
