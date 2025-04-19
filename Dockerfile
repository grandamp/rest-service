# Using `ubi9/openjdk-21-runtime`: https://catalog.redhat.com/software/containers/ubi9/openjdk-21-runtime/6501ce769a0d86945c422d5f
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime
# Modify openssl.cnf to enable FIPS
USER 0
RUN echo -e "\n[algorithm_sect]\ndefault_properties = fips=yes" >> /etc/pki/tls/openssl.cnf
# Image contains `/opt/jboss`, which isn't needed. TODO: Should probably check to see if a package or two should be removed.
RUN /bin/rm -rf /opt/jboss
# Setup App; copy code, install dependencies, set working dir
RUN /usr/bin/mkdir /opt/vss
ADD target/rest-service-eb.jar /opt/vss/lib/
RUN /usr/bin/chown -R 1001 /opt/vss
USER 1001
# Run App
#ENTRYPOINT ["/bin/bash"]  
ENTRYPOINT ["/usr/bin/java", "-Xms8192m", "-Xmx10240m", "-jar", "/opt/vss/lib/rest-service-eb.jar"]
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

# sudo docker build -t vss .
# 
# Ensure `crldata` repository Docker volume exists and contains .crl files
#
# sudo docker run -it --network host --mount source=crldata,destination=/opt/vss/crls vss:latest
# 

