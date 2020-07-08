#!/bin/sh

APN=$(/legato/systems/current/bin/cm data | grep APN:)
APN=${APN#"APN:"}
echo $APN