#!/bin/bash

cd ${project.basedir}

source ${HOME}/legato/packages/legato.sdk.latest/resources/configlegatoenv
export ODA_RESOURCES=${project.build.directory}/${project.artifactId}-${project.version}

mkapp -t wp85 \
 -o target -w target/legato \
  ${ODA_RESOURCES}/oda.adef