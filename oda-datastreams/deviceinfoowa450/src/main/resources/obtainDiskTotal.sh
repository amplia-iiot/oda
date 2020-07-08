#!/bin/sh
# Launch Disk Usage Getter Script

DISKTOTAL=$(df -m | grep "/$" | grep -o "[0-9]*" | grep -o "[1-9]*" | head -n 1)
echo "$DISKTOTAL"