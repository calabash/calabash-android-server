#!/usr/bin/env bash

set -e

source bin/log.sh

CALABASH_ANDROID_SERVER_VERSION=$(cat version | tr -d "\n")

SERVER_MANIFEST="server/AndroidManifest.xml"
BACKEND_GRADLE_FILE="server/app/build.gradle"
APK_PATH="app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"
SERVER_APK_PATH="app/build/outputs/apk/androidTest/debug/TestServer.apk"

# $1 is a path to an AndroidManifest.xml
# $2 is the manifest key to inspect
function sdk_version_from_manifest {
  if [ ! -e "${1}" ]; then
    error "Expected "${1}" to exist."
    exit 1
  fi

  local version=$(
    perl -lne "print $& if /${2}=\"\d+\"/" "${1}" | cut -d= -f2 | tr -d \"
  )

  if [ "${version}" = "" ]; then
    error "Could not find key/value pair in file"
    error " key: '${2}'"
    error "file: ${1}"
    exit 1
  fi

  echo -n $version
}

# $1 is a path to an build.gradle
# $2 is the manifest key to inspect
function sdk_version_from_gradle {
  if [ ! -e "${1}" ]; then
    error "Expected "${1}" to exist."
    exit 1
  fi

  local version=$(
    perl -lne "print $& if /${2}\s+\d+/" "${1}" | cut -d ' ' -f2
  )

  if [ "${version}" = "" ]; then
    error "Could not find key/value pair in file"
    error " key: '${2}'"
    error "file: ${1}"
    exit 1
  fi

  echo -n $version
}

banner "Inspecting Manifest Versions"

SERVER_API_LEVEL=$(sdk_version_from_manifest "${SERVER_MANIFEST}" "android:targetSdkVersion")
BACKEND_API_LEVEL=$(sdk_version_from_gradle "${BACKEND_GRADLE_FILE}" "targetSdkVersion")

if [ "${SERVER_API_LEVEL}" != "${BACKEND_API_LEVEL}" ]; then
  error "Expected android:targetSdkVersion to be the same in these files:"
  echo -e "* ${BACKEND_API_LEVEL}\t<= ${BACKEND_GRADLE_FILE}"
  echo -e "* ${SERVER_API_LEVEL}\t<= ${SERVER_MANIFEST}"
  exit 1
fi

ANDROID_API_LEVEL=$SERVER_API_LEVEL

SERVER_MIN_VERSION=$(sdk_version_from_manifest "${SERVER_MANIFEST}" "android:minSdkVersion")
BACKEND_MIN_VERSION=$(sdk_version_from_gradle "${BACKEND_GRADLE_FILE}" "minSdkVersion")

if [ "${SERVER_MIN_VERSION}" != "${BACKEND_MIN_VERSION}" ]; then
  error "Expected android:minSdkVersion to be the same in these files:"
  error "* ${BACKEND_MIN_VERSION}\t<= ${BACKEND_GRADLE_FILE}"
  error "* ${SERVER_MIN_VERSION}\t<= ${SERVER_MANIFEST}"
  exit 1
fi

info "android:targetSdkVersion=${SERVER_API_LEVEL}"
info "   android:minSdkVersion=${SERVER_MIN_VERSION}"

cd server

CMD="./gradlew clean assembleAndroidTest \
  -Pversion=${CALABASH_ANDROID_SERVER_VERSION}"

shell "${CMD}"

$CMD

banner "Done"

info "Built server ${CALABASH_ANDROID_SERVER_VERSION} with command:"

shell "${CMD}"

info "android:targetSdkVersion=${SERVER_API_LEVEL}"
info "   android:minSdkVersion=${SERVER_MIN_VERSION}"

echo "Stripping signature and manifest from server apk..."
zip -q -d "${APK_PATH}" META-INF/\*
zip -q -d "${APK_PATH}" AndroidManifest.xml

mv "${APK_PATH}" "${SERVER_APK_PATH}"

info "Build done."
info "Output ready in: ${SERVER_APK_PATH}"
