#!/bin/sh
# Launch OpenGate Device Agent
# Use -d flag to debug

RESULT=$(cm radio | grep "Signal:")
RSSI=${RESULT#"Signal:"}
echo $RSSI