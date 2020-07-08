#!/bin/sh
# Init SIM entering the PIN if necessary using Legato cm tool
# Usage: initSim.sh PIN

if [ -z "$1" ]
then
  echo "Usage: $0 PIN"
  exit -1
fi

pin=$1

sim_status=$(/legato/systems/current/bin/cm sim | grep -oEi 'LE_SIM_[A-Z]+')

if [ "$sim_status" = "LE_SIM_INSERTED" ]
then
  $(/legato/systems/current/bin/cm sim enterpin $pin)
  # Wait SIM status to be updated
  sleep 1
  sim_status=$(/legato/systems/current/bin/cm sim | grep -oEi 'LE_SIM_[A-Z]+')
fi

if [ "$sim_status" = "LE_SIM_READY" ]
then
  echo "SIM is ready"
  exit 0
else
  echo "Error unblocking SIM. PIN may be incorrect"
  exit -1
fi