#! /usr/bin/env bash

set -e

mkdir -p "libs/build"
cd ..
cd "bin"
find . -name "*.class" | \
  jar cvf Calabash.jar @/dev/stdin
cd ..
mv bin/Calabash.jar integration-tests/libs/build
cd "instrumentation-backend"
cp "libs/robotium-solo-4.3.1.jar" "../integration-tests/libs/build/"
cp "libs/InstrumentationExposed.jar" "../integration-tests/libs/build/"
cp "libs/maps.jar" "../integration-tests/libs/build/"
cd "../integration-tests"
