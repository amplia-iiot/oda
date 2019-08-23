#!/bin/sh
# Launch Disk Usage Getter Script

DISKUSAGE=$(df -h | grep /data | grep -o "[0-9]*%")
echo "$DISKUSAGE"