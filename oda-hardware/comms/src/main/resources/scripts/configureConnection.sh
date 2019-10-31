#!/bin/sh
# Configure data connection with APN required param and USERNAME and PASSWORD optional params using Legato cm tool
# Usage: configureConnection.sh APN [USERNAME PASSWORD]

if [ -z "$1" ]
then
  echo "Usage: $0 APN [USERNAME PASSWORD]"
  exit -1
fi

apn=$1
username=$2
password=$3

$(/legato/systems/current/bin/cm data apn "$apn")

if [ ! -z "$username" ] && [ ! -z "$password" ]
then
  $(/legato/systems/current/bin/cm data auth PAP "$username" "$password")
else
  $(/legato/systems/current/bin/cm data auth none)
fi