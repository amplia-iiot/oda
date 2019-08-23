#!/bin/sh
# Launch All data Getter Script

MODEL=$(cm info device)
IMEI=$(cm info imei)
IMSI=$(cm sim imsi)
ICC=$(cm sim iccid)
RESULT=$(cm radio | grep "Signal:")
RSSI=${RESULT#"Signal:"}
SOFTWARE=$(cat VERSION.txt)
RESULT=$(cm data | grep "APN:")
APN=${RESULT#"APN:"}