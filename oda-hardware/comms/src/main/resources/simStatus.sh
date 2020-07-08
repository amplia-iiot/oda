#!/bin/sh
# Status of the SIM connected to the device. If no sim is connected will be notified.
# Usage: simStatus.sh

echo $(/legato/systems/current/bin/cm sim)