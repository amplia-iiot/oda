#!/bin/sh
# Launch Temperature Value Info Getter Script

TEMPERATURE=$(cm temp all | grep "Power Controller" | grep -o "[0-9]*\>")
TEMPERATURE="$TEMPERATURE C"
echo "$TEMPERATURE"