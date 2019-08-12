#!/usr/bin/env bash

set -e
source log.sh

banner "Building unit-tester.apk"

rm -rf resources/tmp
mkdir -p resources/tmp

DX="${ANDROID_BUILD_TOOLS}/dx"
CALABASH="${PWD}/libs/build/Calabash.jar"
HAMCREST="${PWD}/libs/hamcrest-core-1.3.jar"
JUNIT="${PWD}/libs/junit-4.12.jar"
TESTS="${PWD}/out/CalabashIntegrationTests.jar"

TARGET_APK="${PWD}/unit-tester-new.apk"

cp "resources/unit-tester.apk" "resources/tmp/unit-tester.apk"
info "Staged resources/unit-tester.apk to resources/tmp"
pushd resources/tmp > /dev/null
  unzip -q "unit-tester.apk"
  rm "unit-tester.apk"
  mkdir -p assets
  pushd assets > /dev/null
    info "Installing new jars"
    rm -rf jars
    mkdir jars
    pushd jars > /dev/null
      "${DX}" --dex --output "1_Calabash.jar" "${CALABASH}"
      "${DX}" --dex --output "2_hamcrest-core-1.3.jar" "${HAMCREST}"
      "${DX}" --dex --output "3_junit-4.12.jar" "${JUNIT}"
      "${DX}" --dex --output "4_CalabashIntegrationTests.jar" "${TESTS}"
    popd > /dev/null
  popd > /dev/null
  zip -q "test.apk" -r .
  cp "test.apk" "${TARGET_APK}"
  info "Installed ${TARGET_APK}"
popd > /dev/null

rm -r resources/tmp
