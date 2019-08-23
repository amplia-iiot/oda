#!/bin/sh
# Launch CPU Usage Getter Script

RESULT=$(top -b -n 1 | grep -o '\b.\{4,5\}id')
CPUPERCENTAGE=${RESULT%.?\%id}
CPUPERCENTAGE=$((100-$CPUPERCENTAGE))
echo "$CPUPERCENTAGE%"