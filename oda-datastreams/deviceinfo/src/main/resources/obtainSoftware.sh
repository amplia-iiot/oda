#!/bin/sh
# Launch Software Info Getter Script

SPINREMOTE=$(cat VERSION.txt)
SPINREMOTE="ODA Version: $SPINREMOTE"
LEGATO=$(legato version)
LEGATO="Legato Version: $LEGATO"
echo "$SPINREMOTE"