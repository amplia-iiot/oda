#!/bin/sh
# Launch OpenGate Device Agent

#function to print usage
usage () {
        echo "Usage :"
        echo "run.sh                    --> start ODA"
        echo "run.sh start              --> start ODA"
        echo "run.sh debug              --> start ODA in debug mode"
        echo "run.sh stop               --> stop ODA if it is already running"
        echo "run.sh status             --> check if ODA is already running"
}

# function to get pid of this process if it is already started
get_pid () {
        PID=`ps -ef | grep java | grep apache.felix | grep $CURRENT_PATH | awk '{ print $2 }'`
}

# check if process is still running
check_process_active () {
        if [ `ps -p $PID | grep java | wc -l` = 1 ]
        then
                return 1
        else
                return 0
        fi
}

# function to launch java process that executes ODA
launch_java () {

        # normal parameters
        PARAMS=" -Dname=$CURRENT_PATH -Djava.net.preferIPv4Stack=true -Djava.security.policy=security/dio.policy -Dlogback.configurationFile=conf/logback.xml -Dgosh.args=--nointeractive -Dorg.jline.terminal.dumb=true"

        #add debug mode if -d option present
        if [ "$1" = "-d" ] ; then

                # Port to debug
                DEBUG_PORT=9000
                PARAMS="$PARAMS -agentlib:jdwp=transport=dt_socket,server=y,address=$DEBUG_PORT,suspend=y"
        fi

        # launch java
        java ${PARAMS} -cp 'bin/*' org.apache.felix.main.Main &
}

# function to start ODA
launch_ODA () {
        if ! [ -n "$PID" ]
        then
                launch_java
        else
                echo "Already running with pid $PID"
        fi
}

# function to start ODA in debug mode
launch_ODA_debug () {
        if ! [ -n "$PID" ]
        then
                launch_java -d
        else
                echo "Already running with pid $PID"
        fi
}

# function to stop ODA
stop_ODA () {

        if ! [ -n "$PID" ]
        then
                echo "Not running"
        else
                echo "Stopping process with pid $PID..."

                # first try to kill process correctly
                kill -15 $PID

                # check every second if process hass stopped until the number of seconds indicated
                WAITING_TIME=10

                for time in $(seq 1 $WAITING_TIME)
                do
                        check_process_active
                        if [ $? = 1 ]
                        then
                                #echo "Process is still running"
                                sleep 1
                        else
                                echo "Process stopped gracefully"
                                return 0
                        fi
                done

                # if after X seconds process hasn't stopped, force kill
                check_process_active
                if [ $? = 1 ]
                then
                        echo "Process hasn't stopped gracefully after $WAITING_TIME seconds. Killing forcefully"
                        kill -9 $PID
                fi
        fi
}

# function to check if ODA is running or not
status_ODA () {
        if ! [ -n "$PID" ]
        then
                echo "Not running"
        else
                echo "Running with pid $PID"
        fi
}

################################################

cd $(dirname $0)
cd ..

# get current path to set as process name
CURRENT_PATH=$(pwd)

# call function to get process pid (if it is already running)
get_pid

# in case there are no arguments, assume it is a normal start
if [ $# -eq 0 ] ; then
        launch_ODA
        exit 0
fi

# if there are arguments, check the argument to decide what to do
case "$1" in
        "start" )
                launch_ODA
                ;;
        "debug" )
                launch_ODA_debug
                ;;
        "stop" )
                stop_ODA
                ;;
        "status" )
                status_ODA
                ;;
        *)
                usage
                ;;
esac
