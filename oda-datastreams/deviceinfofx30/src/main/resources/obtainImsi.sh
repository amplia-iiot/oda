#!/bin/sh
# Launch IMSI Getter Script

RESULT=$(cm sim imsi)
IMSI=${RESULT#"IMSI:"}
echo "$IMSI"