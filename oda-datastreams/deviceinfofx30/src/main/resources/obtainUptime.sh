#!/bin/sh
# Launch Uptime Info Getter Script

UPTIME=$(uptime | grep -o "[0-9][0-9]:[0-9][0-9]:[0-9][0-9]")
echo "$UPTIME"