#!/bin/sh
# Launch RSSI Getter Script

RESULT=$(/legato/systems/current/bin/cm radio | grep "Signal:")
RSSI=${RESULT#"Signal:"}
echo "$RSSI"