#!/bin/sh
# Launch Uptime Info Getter Script

SECONDS=$(cat /proc/uptime | grep -o "[0-9]*" | head -n 1)
echo $SECONDS