#!/bin/sh
# Launch IMSI Getter Script

RESULT=$(/legato/systems/current/bin/cm sim imsi)
IMSI=${RESULT#"IMSI:"}
echo "$IMSI"