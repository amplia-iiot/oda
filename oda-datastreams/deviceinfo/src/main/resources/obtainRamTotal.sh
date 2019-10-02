#!/bin/sh
# Launch RAM Total Getter Script

RESULT=$(vmstat -s | grep "total memory")
USEDRAM=$(echo "$RESULT" | grep -o "[0-9]*")
USEDRAM="$USEDRAM"
echo "$USEDRAM"