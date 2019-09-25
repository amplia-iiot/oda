#!/bin/sh
# Launch APN Getter Script

RESULT=$(/legato/systems/current/bin/cm data | grep "APN:")
APN=${RESULT#"APN:"}
echo "$APN"