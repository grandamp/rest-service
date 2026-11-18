#!/bin/bash
#
# For additional debugging info, add: `-Djava.security.debug=certpath+thread+timestamp -D-Djavax.net.debug=all`
# Mitigate CVE-2026-114938 (Java4Shell) by disabling the x86-64 AVX-512 Base64 C2 intrinsic until Temurin 25 publishes a patched image.
#
/opt/java/openjdk/bin/java -XX:-UseBASE64Intrinsics -jar /opt/vss/lib/rest-service-eb.jar --spring.config.name=vss --spring.config.location=file:///opt/vss/ext/conf/
