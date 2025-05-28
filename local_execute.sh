#!/bin/bash
#
# For additional debugging info, add: -Djava.security.debug=certpath -D-Djavax.net.debug=all
#
# mvn clean package spring-boot:repackage; java -Djava.security.debug=certpath -jar target/rest-service-eb.jar --spring.config.name=vss --spring.config.location=file:///opt/vss/ext/conf/
#
mvn clean package spring-boot:repackage; java -jar target/rest-service-eb.jar --spring.config.name=vss --spring.config.location=file:///opt/vss/ext/conf/
