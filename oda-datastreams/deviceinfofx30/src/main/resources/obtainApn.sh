#!/bin/sh
# Launch OpenGate Device Agent
# Use -d flag to debug

RESULT=$(cm data | grep "APN:")
APN=${RESULT#"APN:"}
echo $APN