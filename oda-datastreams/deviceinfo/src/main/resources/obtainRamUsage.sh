#!/bin/sh
# Launch RAM Usage Getter Script

RESULT=$(free -m)
TOTALRAM=$(echo "$RESULT" | grep -o 'Mem:\s*[0-9]*' | grep -o '[0-9]*')
USEDRAM=$(echo "$RESULT" | grep -o 'Mem:\s*[0-9]*\s*[0-9]*' | grep -o '[0-9]*' | sed -n 2p)
USEDPERCENTAGE=$(($USEDRAM*100/$TOTALRAM))
echo "$USEDPERCENTAGE"
