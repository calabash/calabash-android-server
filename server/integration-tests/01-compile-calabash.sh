#!/usr/bin/env bash

set -e
source ./log.sh

banner "Creating and Installing Calabash.jar"

rm -rf "libs/build"
mkdir -p "libs/build"

SERVER_DIR="../app/build/intermediates/javac/debugAndroidTest/compileDebugAndroidTestJavaWithJavac/classes"
info "Creating Calabash.jar from *.class files"
pushd "${SERVER_DIR}" > /dev/null
  find . -name "*.class" | jar cf Calabash.jar @/dev/stdin
popd > /dev/null

mv "${SERVER_DIR}/Calabash.jar" "libs/build"
info "Installed libs/build/Calabash.jar"
