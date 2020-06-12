#!/bin/sh
# Launch CPU Usage Getter Script

RESULT=$(top -b -n 1 | grep -o '\b.\{4,5\}id')
CPUPERCENTAGE=$(echo $RESULT | grep -o "[0-9]*" | head -n 1)
CPUPERCENTAGE=$((100-$CPUPERCENTAGE))
echo "$CPUPERCENTAGE"