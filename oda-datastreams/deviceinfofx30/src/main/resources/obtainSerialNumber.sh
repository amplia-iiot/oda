#!/bin/sh
# Launch Serial Number Getter Script

SERIAL=$(cm info fsn)
echo "$SERIAL"