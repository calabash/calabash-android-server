#! /usr/bin/env bash

set -e

integrationTestsDir=$(pwd)
calabashServerDir="../app/build/intermediates/classes/androidTest/debug"

mkdir -p "libs/build"

cd "${calabashServerDir}"
find . -name "*.class" | \
  jar cvf Calabash.jar @/dev/stdin

cd "${integrationTestsDir}"
mv "${calabashServerDir}/Calabash.jar" libs/build
