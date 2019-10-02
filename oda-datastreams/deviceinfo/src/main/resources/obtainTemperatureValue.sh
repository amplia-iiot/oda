#!/bin/sh
# Launch Temperature Value Info Getter Script

TEMPERATURE=$(cat /sys/class/thermal/thermal_zone2/temp)
TEMPERATURE=$((TEMPERATURE / 1000))
echo "$TEMPERATURE"