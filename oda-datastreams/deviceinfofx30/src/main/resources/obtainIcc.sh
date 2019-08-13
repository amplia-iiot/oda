#!/bin/sh
# Launch OpenGate Device Agent
# Use -d flag to debug

RESULT=$(cm sim iccid)
ICC=${RESULT#"ICC:"}
echo ICC