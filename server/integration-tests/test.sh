#!/usr/bin/env bash

set -e
source log.sh

export ANDROID_PLATFORM="${ANDROID_HOME}/platforms/android-28"
if [ ! -d "${ANDROID_PLATFORM}" ]; then
  error "Missing ${ANDROID_PLATFORM}"
  error "Install with Android Studio"
  exit 1
fi

export ANDROID_BUILD_TOOLS="${ANDROID_HOME}/build-tools/28.0.3"
if [ ! -d "${ANDROID_BUILD_TOOLS}" ]; then
  error "Missing ${ANDROID_BUILD_TOOLS}"
  error "Install with Android Studio"
  exit 1
fi

info "Using JAVA_HOME=${JAVA_HOME}"
info "Using ANDROID_HOME=${ANDROID_HOME}"
info "Using ANDROID_PLATFORM=${ANDROID_PLATFORM}"
info "Using ANDROID_BUILD_TOOLS=${ANDROID_BUILD_TOOLS}"

# Stage calabash-android sources.  Build the latest TestServer.apk
# and install into the calabash-android sources.  TestServer.apk
# will be used later in the tests.  Passing --skip-build will
# skip a rebuild of the TestServer.apk
./00-install-latest-test-server.sh ${1}

# Create a .jar file from Calabash sources - used to compile server unit tests.
./01-compile-calabash.sh

# Create a .jar file from integration test sources.
./02-compile-tests.sh

# Build unit-tester.apk
./03-build-test-apk.sh

# Run the tests
./04-run-tests.sh
