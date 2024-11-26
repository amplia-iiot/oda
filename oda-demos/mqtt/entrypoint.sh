#!/bin/sh

# Copy deploy and configuration to persistant volume
mkdir persist/deploy
cp -Rf deploy/* persist/deploy
mkdir persist/configuration
cp -Rf configuration/* persist/configuration
mkdir persist/rules

rm -rf deploy
rm -rf configuration
rm conf/config.properties

PARAMS=" -Dname=$CURRENT_PATH -Djava.net.preferIPv4Stack=true -Djava.security.policy=security/dio.policy -Dfelix.config.properties=file:confDocker/config.properties -Dlogback.configurationFile=conf/logback.xml -Dgosh.args=--nointeractive -Dorg.jline.terminal.dumb=true"

# launch java
java ${PARAMS} -cp 'bin/*' org.apache.felix.main.Main
