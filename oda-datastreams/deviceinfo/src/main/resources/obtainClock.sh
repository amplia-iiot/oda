#!/bin/sh
# Launch Date Getter Script

DATETIME=$(date '+%Y-%m-%d,%H:%M:%S,%Z,%:z')
DATE=$(echo "$DATETIME" | cut -d "," -f 1)
TIME=$(echo "$DATETIME" | cut -d "," -f 2)
TIMEZONE=$(echo "$DATETIME" | cut -d "," -f 3)
# TIMEZONE_OFFSET=$(echo "$DATETIME" | cut -d "," -f 4)

DST=0
# there are two ways to determine if dst is active or not
# this is valid for timezone = Europe/Madrid
# if timezone is different, this must be adjusted

# if $TIMEZONE = CEST -> DST = 1, if $TIMEZONE = CET -> DST = 0
if [ "$TIMEZONE" = "CEST" ]; then DST=1; fi

# if $TIMEZONE_OFFSET = +02:00 -> DST = 1, if $TIMEZONE_OFFSET = +01:00 -> DST = 0
# if [ "$TIMEZONE_OFFSET" = "+02:00" ]; then DST=1; fi

echo "{\"date\" : \"$DATE\", \"time\" : \"$TIME\", \"timezone\" : \"$TIMEZONE\", \"dst\" : $DST}"
