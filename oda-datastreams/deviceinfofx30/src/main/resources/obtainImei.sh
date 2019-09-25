#!/bin/sh
# Launch IMEI Getter Script

IMEI=$(/legato/systems/current/bin/cm info imei)
echo "$IMEI"