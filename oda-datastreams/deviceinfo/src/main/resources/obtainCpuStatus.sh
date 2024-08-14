#!/bin/sh
# Launch CPU Status Getter Script

RESULT=$(top -b -n 1 | grep -o 'ni, [0-9]*.[0-9]* id')
CPUPERCENTAGE=$(echo $RESULT | grep -o "[0-9]*" | head -n 1)
CPUPERCENTAGE=$((100-$CPUPERCENTAGE))
if [ "$CPUPERCENTAGE" -ge 80 ]
then
  echo OVERLOAD
elif [ "$CPUPERCENTAGE" -ge 60 ]
then
  echo STRESSED
elif [ "$CPUPERCENTAGE" -ge 20 ]
then
  echo WORKING
elif [ "$CPUPERCENTAGE" -ge 5 ]
then
  echo IDLE/WORKING
else
  echo IDLE
fi