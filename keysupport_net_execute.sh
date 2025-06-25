#!/bin/bash
#
# For additional debugging info, add: `-Djava.security.debug=certpath+thread+timestamp -D-Djavax.net.debug=all`
#
export BASE_URI=https://home.keysupport.net
/usr/bin/java -jar /opt/vss/lib/rest-service-eb.jar --spring.config.name=vss --spring.config.location=file:///opt/vss/ext/conf/
