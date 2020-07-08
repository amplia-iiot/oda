#!/bin/sh
# Launch Disk Usage Getter Script

DISKUSAGE=$(df -m | grep "/$" | grep -o "[0-9]*%" | grep -o "[0-9]*" | head -n 1)
echo "$DISKUSAGE"