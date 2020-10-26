#!/bin/sh
# Launch OpenGate Device Agent
# Use -d flag to debug

cd $(dirname $0)
cd ..

# Port to debug
DEBUG_PORT=8000

DEBUG_OPTION=false
ACTIVE_OPTION=false

while test $# -gt 0
do
    echo $1
    case "$1" in
        -d|--debug)
            DEBUG_OPTION=true
            echo "debug case"
            ;;
        -p|--port)
          DEBUG_PORT=$2
            echo "port case"
            shift
            ;;
        -a|--active)
            ACTIVE_OPTION=true
            echo "active case"
            ;;
        *) echo "argument $1"
            ;;
    esac
    shift
done

PARAMS=" -Djava.net.preferIPv4Stack=true -Djava.security.policy=security/dio.policy -Dlogback.configurationFile=conf/logback.xml"

if [ "$ACTIVE_OPTION" = "false" ]
then
    echo "nointeractive"
    PARAMS="$PARAMS -Dgosh.args=--nointeractive -Dorg.jline.terminal.dumb=true"
fi

if [ "$DEBUG_OPTION" = "true" ]
then
    echo "debug"
    PARAMS="$PARAMS -agentlib:jdwp=transport=dt_socket,server=y,address=$DEBUG_PORT,suspend=y"
fi

if [ "$ACTIVE_OPTION" = "false" ]
then
    echo "nointeractive"
    java ${PARAMS} -cp 'bin/*' org.apache.felix.main.Main &
else
    echo "interactive"
    java ${PARAMS} -cp 'bin/*' org.apache.felix.main.Main
fi
