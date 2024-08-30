#!/bin/sh
# Launch Temperature Value Info Getter Script

TEMPERATURE=$(cat /sys/class/thermal/thermal_zone2/temp)
if [ "$TEMPERATURE" = "" ]
then
  unset $TEMPERATURE
else
  TEMPERATURE=$((TEMPERATURE / 1000))
fi
echo "$TEMPERATURE"
