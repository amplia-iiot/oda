#!/bin/sh
# Launch Serial Number Getter Script

SERIAL=$(/legato/systems/current/bin/cm info fsn)
echo "$SERIAL"