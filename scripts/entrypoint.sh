#!/bin/bash
#
# For additional debugging info, add: `-Djava.security.debug=certpath+thread+timestamp -Djavax.net.debug=all`
#
exec /opt/java/openjdk/bin/java \
  -XX:+UseZGC \
  -XX:MaxRAMPercentage=75.0 \
  ${JAVA_OPTS:-} \
  -jar /opt/vss/lib/rest-service-eb.jar \
  --spring.config.name=vss \
  --spring.config.location=file:///opt/vss/ext/conf/
