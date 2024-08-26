#!/bin/sh
# Launch RAM Total Getter Script

RESULT=$(top -b -n 1)
TOTALRAM=$(echo "$RESULT" | grep -o '\.*[0-9]* total' -m 2 | sed -n 2p | grep -o '[0-9]*')
echo "$TOTALRAM"