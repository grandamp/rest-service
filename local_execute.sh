#!/bin/bash
#
# For additional debugging info, add: `-Djava.security.debug=certpath+thread+timestamp -D-Djavax.net.debug=all`
#
/usr/local/bin/mvn clean package spring-boot:repackage; java -Djava.security.debug=certpath+thread+timestamp -jar target/rest-service-eb.jar --spring.config.name=vss --spring.config.location=file:///opt/vss/ext/conf/
