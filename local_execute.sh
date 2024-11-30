#!/bin/bash
export BASE_URI=http://localhost:5000
#
# If testing with memcached on localhost, and making caching logic changes; it is a good idea to flush the cache:
#
# - `echo 'flush_all' | netcat 127.0.0.1 11211`
#
export MEMCACHED_CNF=127.0.0.1
#export MEMCACHED_CNF=server.lan
#
# For additional debugging info, add: -Djava.security.debug=certpath -D-Djavax.net.debug=all
#
# mvn clean package spring-boot:repackage; java -Djava.security.debug=certpath -jar target/rest-service-eb.jar >out 2>&1
mvn clean package spring-boot:repackage; java -Djava.security.debug=certpath -jar target/rest-service-eb.jar
