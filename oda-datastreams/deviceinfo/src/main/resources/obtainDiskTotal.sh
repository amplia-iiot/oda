#!/bin/sh
# Launch Disk Usage Getter Script

DISKTOTAL=$(df -m | grep "\s*[0-9]*\s*[0-9]*\s*[0-9]*\s*[0-9]*%\s*\/\$" | grep -o " [0-9]*" | grep -o "[0-9]*" | head -n 1)
echo "$DISKTOTAL"
