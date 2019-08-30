#!/bin/sh
# Launch Uptime Info Getter Script

UPTIME=$(uptime | grep -o "up [0-9]*")
UPTIME=$(echo $UPTIME | grep -o "[0-9]*")
echo "$UPTIME"