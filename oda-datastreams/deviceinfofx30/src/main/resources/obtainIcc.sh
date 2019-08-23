#!/bin/sh
# Launch ICC Getter Script

RESULT=$(cm sim iccid)
ICC=${RESULT#"ICCID:"}
echo "$ICC"