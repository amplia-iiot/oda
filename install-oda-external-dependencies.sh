#!/bin/bash

if [ ! -d ${HOME}/.m2/repository/com/automatak/dnp3 ] && [ ! -d ${HOME}/.m2/repository/com/diozero/libdiozero-system-utils ] && [ ! -d ${HOME}/.m2/repository/net/java/openjdk ]; then
    cd oda-externaldependencies
    mvn install
fi

