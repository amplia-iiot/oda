#!/bin/sh
# Connect using configured data connection using Legato cm tool. The default connection timeout is 60 seconds
# Usage: connect.sh [TIMEOUT]

DEFAULT_TIMEOUT=60

timeout=$1
if [ ! -e "$timeout" ]
then
  timeout=$DEFAULT_TIMEOUT
fi
$(/legato/systems/current/bin/cm data connect $timeout)
