#!/bin/sh
# Launch RAM Usage Getter Script

RESULT=$(vmstat -s)
TOTALRAM=$(echo "$RESULT" | grep "total memory")
TOTALRAM=$(echo "$TOTALRAM" | grep -o "[0-9]*")
USEDRAM=$(echo "$RESULT" | grep "used memory")
USEDRAM=$(echo "$USEDRAM" | grep -o "[0-9]*")
USEDPERCENTAGE=$(($USEDRAM*100/$TOTALRAM))
echo "$USEDPERCENTAGE"