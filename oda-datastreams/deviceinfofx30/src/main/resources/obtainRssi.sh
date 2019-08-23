#!/bin/sh
# Launch RSSI Getter Script

RESULT=$(cm radio | grep "Signal:")
RSSI=${RESULT#"Signal:"}
echo "$RSSI"