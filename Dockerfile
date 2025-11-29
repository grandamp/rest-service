# Using `eclipse-temurin:25-jre-ubi10-minimal`: https://hub.docker.com/layers/library/eclipse-temurin/25-jre-ubi10-minimal/
FROM eclipse-temurin:25-jre-ubi10-minimal

# Switch to user 0
USER 0

# Modify openssl.cnf to enable FIPS - Uncomment when OpenSSL 3.5 FIPS is available in the base image to enforce FIPS
# RUN echo -e "\n[algorithm_sect]\ndefault_properties = fips=yes" >> /etc/pki/tls/openssl.cnf

# Setup App; copy code, install dependencies, set working dir
RUN /usr/bin/mkdir /opt/vss
ADD target/rest-service-eb.jar /opt/vss/lib/
ADD keysupport_net_execute.sh /opt/vss/
RUN /usr/bin/chown -R 1000 /opt/vss
RUN /usr/bin/chmod 700 /opt/vss/keysupport_net_execute.sh

# Switch to user 1000
USER 1000

# Run App
ENTRYPOINT ["/opt/vss/keysupport_net_execute.sh"]

# =================================
# Container meta information
# ---------------------------------
LABEL build-date=$BUILD_DATE
LABEL description="Validation Service"
LABEL maintainer="Todd E. Johnson <tejohnson@keysupport.net>"
LABEL name="Validation Service"
LABEL usage="https://github.com/grandamp/rest-service/blob/main/README.md"
LABEL vcs-ref=$PIPELINE_ID
LABEL vcs-url="https://github.com/grandamp/rest-service"
LABEL vendor="https://keysupport.net"

# This example uses a bind mount that assumes /opt/vss/ext is available on the host system with mixed r/w privs for user 1000
# $ docker build -t vss .
# $ docker run --restart unless-stopped -it --network host -v /opt/vss/ext:/opt/vss/ext:z --cap-add=CAP_AUDIT_WRITE vss:latest

