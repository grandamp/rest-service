#!/bin/bash
export BASE_URI=http://localhost:5000
#
# For additional debugging info, add: -Djava.security.debug=certpath -D-Djavax.net.debug=all
#
# mvn clean package spring-boot:repackage; java -Djava.security.debug=certpath -jar target/rest-service-eb.jar >out 2>&1
mvn clean package spring-boot:repackage; java -Djava.security.debug=certpath -jar target/rest-service-eb.jar
