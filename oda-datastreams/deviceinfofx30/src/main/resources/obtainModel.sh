#!/bin/sh
# Launch Model Getter Script

MODEL=$(/legato/systems/current/bin/cm info device)
echo "$MODEL"