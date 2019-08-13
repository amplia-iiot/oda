#!/bin/sh
# Launch OpenGate Device Agent
# Use -d flag to debug

RESULT=$(cm sim imsi)
IMSI=${RESULT#"IMSI:"}
echo $IMSI