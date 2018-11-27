#!/bin/sh
# Launch OpenGate Device Agent
# Use -d flag to debug

cd $(dirname $0)
cd ..

# Port to debug
DEBUG_PORT=8000

PARAMS=" -Dorg.ops4j.pax.logging.DefaultServiceLog.level=WARN"

if [ "$1" = "-d" ]
then
    PARAMS="$PARAMS -agentlib:jdwp=transport=dt_socket,server=y,address=$DEBUG_PORT,suspend=y"
fi

java $PARAMS -cp 'bin/*' -Dlogback.configurationFile=conf/logback.xml org.apache.felix.main.Main
