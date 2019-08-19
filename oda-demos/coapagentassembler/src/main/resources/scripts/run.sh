#!/bin/sh
# Launch OpenGate Device Agent
# Use -d flag to debug

cd $(dirname $0)
cd ..

# Port to debug
DEBUG_PORT=8000

PARAMS=" -Djava.net.preferIPv4Stack=true -Djava.security.policy=security/dio.policy -Dlogback.configurationFile=conf/logback.xml"

if [ "$1" = "-d" ]
then
    PARAMS="$PARAMS -agentlib:jdwp=transport=dt_socket,server=y,address=$DEBUG_PORT,suspend=y"
fi

java ${PARAMS} -cp 'bin/*' org.apache.felix.main.Main