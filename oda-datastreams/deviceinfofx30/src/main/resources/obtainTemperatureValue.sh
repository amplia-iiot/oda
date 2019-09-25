#!/bin/sh
# Launch Temperature Value Info Getter Script

TEMPERATURE=$(/legato/systems/current/bin/cm temp all | grep "Power Controller" | grep -o "[0-9]*\>")
TEMPERATURE="$TEMPERATURE"
echo "$TEMPERATURE"