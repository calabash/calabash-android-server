#!/usr/bin/env bash

set -e
source log.sh

banner "Building CalabashIntegrationTests.jar"

rm -rf ./tmp
mkdir -p ./tmp

rm -rf ./out
mkdir ./out

info "Compiling from ./src"
find ./src -name "*.java" | \
  javac -cp \
  "${ANDROID_PLATFORM}/android.jar:libs/build/Calabash.jar:libs/junit-4.12.jar" \
  -d tmp @/dev/stdin

pushd tmp > /dev/null
  find . -name "*.class" | jar cf CalabashIntegrationTests.jar @/dev/stdin
popd > /dev/null

mv "tmp/CalabashIntegrationTests.jar" "out/CalabashIntegrationTests.jar"
info "Installed out/CalabashIntegrationTests.jar"
