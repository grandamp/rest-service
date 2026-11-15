#!/bin/bash
/usr/bin/docker ps -q --filter ancestor=ghcr.io/grandamp/rest-service:latest | xargs -r /usr/bin/docker rm -f
/usr/bin/docker system prune -f
/usr/bin/docker pull ghcr.io/grandamp/rest-service:latest
/usr/bin/docker run -d --restart unless-stopped --network host -e BASE_URI=https://keysupport.net -v /opt/vss/ext:/opt/vss/ext ghcr.io/grandamp/rest-service:latest
