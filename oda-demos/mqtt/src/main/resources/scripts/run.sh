#!/bin/sh
# Launch OpenGate Device Agent
# Use -d flag to debug

cd $(dirname $0)
cd ..

# get current path to set as process name
path=$(pwd)

# function to get pid of this process is it is already started
get_pid () {
   pid=`ps -ef | grep java | grep apache.felix | grep $path | awk '{ print $2 }'`
}

# function to launch java process that executes ODA
launch_java () {

        # Port to debug
        DEBUG_PORT=8000

        # normal parameters
        PARAMS=" -Dname=$path -Djava.net.preferIPv4Stack=true -Djava.security.policy=security/dio.policy -Dlogback.configurationFile=conf/logback.xml -Dgosh.args=--nointeractive -Dorg.jline.terminal.dumb=true"

        # if we want to activate debug mode
        if [ "$1" = "-d" ] ; then
                PARAMS="$PARAMS -agentlib:jdwp=transport=dt_socket,server=y,address=$DEBUG_PORT,suspend=y"
        fi

        # launch java
        java ${PARAMS} -cp 'bin/*' org.apache.felix.main.Main &
}

# call function to get pid
get_pid

# check if we want to start, stop or check the status of the process
case "$1" in
   "start" )
        if ! [ -n "$pid" ]
        then
                launch_java "$2"
        else
                echo "Already running with pid $pid"
        fi
        ;;

   "stop" )
        if ! [ -n "$pid" ]
        then
                echo "Not running"
        else
                echo "Stopping process with pid $pid"
                kill -9 $pid
        fi
        ;;

   "status" )
        if ! [ -n "$pid" ]
        then
                echo "Not running"
        else
                echo "Running with pid $pid"
        fi
        ;;

   *)
        echo "Bad usage. Parameters accepted are 'start', 'start -d' (for debugging), 'stop' and 'status'"
esac