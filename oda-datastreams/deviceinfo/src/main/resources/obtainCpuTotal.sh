#!/bin/sh
# Launch CPU Usage Getter Script

RESULT=$(cat /proc/cpuinfo | grep "cpu cores" | head -n 1)
CPUTOTAL=$(echo "$RESULT" | grep -o "[0-9]*\>")
echo "$CPUTOTAL"