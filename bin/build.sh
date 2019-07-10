#!/usr/bin/env bash

set -e
source bin/log.sh

info "Using JAVA_HOME=${JAVA_HOME}"
info "Using ANDROID_HOME=${ANDROID_HOME}"

APK=server/app/build/outputs/apk/androidTest/debug/TestServer.apk

if [ "${1}" = "--verify-only" ]; then
  info "Skipping build step; will verify ${APK}"
else
  pushd server > /dev/null
    ./gradlew clean assembleAndroidTest
  popd > /dev/null
fi

echo ""
info "Used JAVA_HOME=${JAVA_HOME}"
info "Used ANDROID_HOME=${ANDROID_HOME}"

banner "Verifying Contents of TestServer.apk"

if [ $(unzip -l "${APK}" | tail -n 1 | awk '{print $2}') != "10" ]; then
  error "Expected 10 files in TestServer.apk:"
  echo ""
  error "Expected:"
  error "resources.arsc"
  error "junit/runner/logo.gif"
  error "junit/runner/smalllogo.gif"
  error "classes.dex"
  error "LICENSE-junit.txt"
  error "assets/calabash.js"
  error "assets/AndroidManifest.xml"
  error "assets/version"
  error "assets/actions"
  error "com/jayway/android/robotium/solo/RobotiumWeb.js"
  error ""
  error "Found:"

  unzip -l "${APK}"

  error "Failing the build"
  exit 1
else
  info "Found 10 files:"
  unzip -l "${APK}"
  info "Done"
fi
