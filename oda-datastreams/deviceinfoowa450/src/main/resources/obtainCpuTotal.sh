#!/bin/sh
# Launch CPU Usage Getter Script

RESULT=$(lscpu | grep "CPU(s)" | head -n 1)
CPUTOTAL=$(echo "$RESULT" | grep -o "[0-9]*\>")
echo "$CPUTOTAL"