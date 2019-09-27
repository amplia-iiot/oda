#!/bin/sh
# Launch Disk Usage Getter Script

DISKTOTAL=$(df -m | grep /data | grep -o "[0-9]*" | head -n 1)
echo "$DISKTOTAL"