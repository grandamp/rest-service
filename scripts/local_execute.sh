#!/bin/bash
#
# For additional debugging info, add: `-Djava.security.debug=certpath+thread+timestamp -D-Djavax.net.debug=all`
#
export BASE_URI=http://localhost:5000
/usr/local/bin/mvn clean package spring-boot:repackage; java -Djava.security.debug=certpath+thread+timestamp -jar target/rest-service-eb.jar --spring.config.name=vss --spring.config.location=file:///opt/vss/ext/conf/
