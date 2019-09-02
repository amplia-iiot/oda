#!/bin/sh
# Launch Temperature Status Info Getter Script

TEMPERATURE=$(cat /sys/class/thermal/thermal_zone2/temp)
TEMPERATURE=$((TEMPERATURE / 1000))
if [ "$TEMPERATURE" -ge 50 ]
then
  echo HIGH_CRITICAL
elif [ "$TEMPERATURE" -ge 25 ]
then
  echo HIGH_WARNING
elif [ "$TEMPERATURE" -le "-50" ]
then
  echo LOW_CRITICAL
elif [ "$TEMPERATURE" -le "-25" ]
then
  echo LOW_WARNING
else
  echo NORMAL
fi