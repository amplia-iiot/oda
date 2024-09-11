#!/bin/sh
# Launch RAM Total Getter Script

RESULT=$(free -m)
TOTALRAM=$(echo "$RESULT" | grep -o 'Mem:\s*[0-9]*' | grep -o '[0-9]*')
echo "$TOTALRAM"
