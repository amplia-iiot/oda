#!/bin/sh
# Check if the apn configured in legato is the expected by the oda.
# Usage: checkApn.sh APN PATH. APN is the apn expected and PATH is the path to the oda directory.

{ echo ""; echo "Executing new instance of the Check APN script"; echo "Executing at time:"; } >> "$2/../log/apn_log.txt"
date >> "$2/../log/apn_log.txt"

if grep -w -q "^$1$" "$2/../configuration/apns_to_redirect.txt"
then
        RESULT=$(/legato/systems/current/bin/cm data | grep "APN:")
        APN=${RESULT#"APN:        "}

        { echo "RUNNING"; echo "ACTUAL APN $APN"; echo "CONFIG APN $1"; } >> "$2/../log/apn_log.txt"

        if [ "$APN" == "$1" ]
        then
                echo "EQUALS, CHANGING TO GENERAL APN" >> "$2/../log/apn_log.txt"
                /legato/systems/current/bin/cm data apn ba.amx
                echo REBOOTING >> "$2/../log/apn_log.txt"
                /sbin/reboot
                echo "REDIRECTING"
        else
                echo "NEQUALS, CHANGIN TO CONFIGURED APN" >> "$2/../log/apn_log.txt"
                /legato/systems/current/bin/cm data apn "$1"
                echo "REDIRECTED"
        fi
else
        echo "APN DOESN'T NEED TO BE REDIRECTED" >> "$2/../log/apn_log.txt"
        echo "CHANGING NORMALLY THE APN TO $1" >> "$2/../log/apn_log.txt"
        /legato/systems/current/bin/cm data apn "$1"
        echo "DIRECT"
fi