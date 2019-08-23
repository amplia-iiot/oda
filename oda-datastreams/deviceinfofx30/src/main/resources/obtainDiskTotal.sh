#!/bin/sh
# Launch Disk Usage Getter Script

DISKTOTAL=$(df -h | grep /data | grep -o "[0-9]*.[0-9][K,M,G,\b]" | head -n 1)
echo "$DISKTOTAL"