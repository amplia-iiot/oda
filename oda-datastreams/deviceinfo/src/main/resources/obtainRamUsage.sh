#!/bin/sh
# Launch RAM Usage Getter Script

RESULT=$(top -b -n 1)
TOTALRAM=$(echo "$RESULT" | grep -o '\.*[0-9]* total' -m 2 | sed -n 2p | grep -o '[0-9]*')
USEDRAM=$(echo "$RESULT" |  grep -o '\.*[0-9]* used' -m 1 | grep -o '[0-9]*')
USEDPERCENTAGE=$(($USEDRAM*100/$TOTALRAM))
echo "$USEDPERCENTAGE"