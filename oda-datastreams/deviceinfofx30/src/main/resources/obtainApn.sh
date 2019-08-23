#!/bin/sh
# Launch APN Getter Script

RESULT=$(cm data | grep "APN:")
APN=${RESULT#"APN:"}
echo "$APN"