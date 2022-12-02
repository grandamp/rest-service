#!/bin/bash
export BASE_URI=http://localhost:5000
export MEMCACHED_CNF=127.0.0.1
#
# Specific to my AWS Account, with AWS SDK using AWS CLI Config locally
#
export S3_BUCKET=elasticbeanstalk-us-east-1-216896468348
#
# For additional debugging info, add: -Djava.security.debug=certpath -D-Djavax.net.debug=all
#
mvn clean package spring-boot:repackage; java -Djava.security.debug=certpath -jar target/rest-service-eb.jar >out 2>&1
