#!/bin/sh
# Launch Software Info Getter Script

SPINREMOTE=$(cat VERSION.txt)
SPINREMOTE="ODA Version: $SPINREMOTE"
RESULT=$(/legato/systems/current/bin/fwupdate query)
FIRMWARE=$(echo "$RESULT" | grep "Firmware")
BOOTLOADER=$(echo "$RESULT" | grep "Bootloader")
LINUX=$(echo "$RESULT" | grep "Linux")
FX30=$(echo "$RESULT" | grep "FX30")
LEGATO=$(/legato/systems/current/bin/legato version)
LEGATO="Legato Version: $LEGATO"
echo "$FIRMWARE && $BOOTLOADER && $LINUX && $FX30 && $LEGATO && $SPINREMOTE"