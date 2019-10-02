#!/bin/sh
# Launch ICC Getter Script

RESULT=$(/legato/systems/current/bin/cm sim iccid)
ICC=${RESULT#"ICCID:"}
echo "$ICC"