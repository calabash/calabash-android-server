#! /usr/bin/env bash

set -e

source log.sh

# Switch Java to v8 in runtime from current context
. switch_java_version.sh

GEM_DIR=calabash-android

rm -rf "${GEM_DIR}"

# Testing locally; rebuild server from sources
if [ "${JENKINS_HOME}" != "" ]; then
  info "Rebuilding TestServer.apk"
  (cd .. && ./gradlew clean assembleAndroidTest)
else
  info "Detected Jenkins environment; will not rebuild TestServer.apk"
  info "Assuming TestServer.apk is built in previous step"
fi

TEST_SERVER_APK="../app/build/outputs/apk/androidTest/debug/TestServer.apk"
TEST_SERVER_MANIFEST="../AndroidManifest.xml"

if [ -e "${TEST_SERVER_APK}" ]; then
  info "Will install new TestServer.apk in calabash-android gem"
  info "  - ${TEST_SERVER_APK}"
fi

if [ "${CALABASH_ANDROID_PATH}" != "" ]; then
  info "Detected CALABASH_ANDROID_PATH variable is set"
  if [ -d "${CALABASH_ANDROID_PATH}" ]; then
    ln -s "${CALABASH_ANDROID_PATH}" "${GEM_DIR}"
  else
    error "Expected calabash-android gem sources at path"
    error "  - ${CALABASH_ANDROID_PATH}"
    error "but found no directory"
    exit 1
  fi
elif [ -d "../../../calabash-android" ]; then
  info "Detected local calabash-android gem sources:"
  info "  - ../../../calabash-android"
  info "Will use these sources for tests; creating a symlink @ calabash-android"
  ln -s "../../../calabash-android" "${GEM_DIR}"
else
  info "Cloning calabash-android develop branch"
  git clone https://github.com/calabash/calabash-android.git "${GEM_DIR}"
fi

info "Installing new TestServer.apk into gem/calabash-android"
cp "${TEST_SERVER_APK}" "${GEM_DIR}/ruby-gem/lib/calabash-android/lib/"
info "Install new AndroidManifest.xml into gem/calabash-android"
cp "${TEST_SERVER_MANIFEST}" "${GEM_DIR}/ruby-gem/lib/calabash-android/lib/"

./compile_calabash.sh
./compile_tests.sh
./build_test_apk.sh
./setup_unit_tests.sh

(
  cd calabash-test-suite
  bundle update
  SKIP_VERSION_CHECK=1 bundle exec calabash-android run \
    unittest.apk \
    --format pretty \
    --format junit --out test_report
)
