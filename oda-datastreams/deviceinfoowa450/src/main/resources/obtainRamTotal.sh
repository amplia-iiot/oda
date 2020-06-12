#!/bin/sh
# Launch RAM Total Getter Script

RESULT=$(vmstat -s | grep "total memory")
TOTALRAM=$(echo "$RESULT" | grep -o "[0-9]*")
TOTALRAM="$TOTALRAM"
echo "$TOTALRAM"