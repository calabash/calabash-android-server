#!/usr/bin/env bash

set -e

source bin/log.sh

CALABASH_ANDROID_SERVER_VERSION=$(cat version | tr -d "\n")

SERVER_MANIFEST="server/AndroidManifest.xml"
BACKEND_MANIFEST="server/instrumentation-backend/src/main/AndroidManifest.xml"


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

# $1 is a path to an AndroidManifest.xml
function target_sdk {
  if [ ! -e "${1}" ]; then
    error "Expected "${1}" to exist."
    exit 1
  fi

  local version=$(sdk_version_from_manifest "${1}" "android:targetSdkVersion")
  echo -n $version
}

# $1 is a path to an AndroidManifest.xml
function min_sdk {
  if [ ! -e "${1}" ]; then
    error "Expected "${1}" to exist."
    exit 1
  fi

  local version=$(sdk_version_from_manifest "${1}" "android:minSdkVersion")
  echo -n $version
}

banner "Inspecting Manifest Versions"

SERVER_API_LEVEL=$(target_sdk "${SERVER_MANIFEST}")
BACKEND_API_LEVEL=$(target_sdk "${BACKEND_MANIFEST}")

if [ "${SERVER_API_LEVEL}" != "${BACKEND_API_LEVEL}" ]; then
  error "Expected android:targetSdkVersion to be the same in these files:"
  echo -e "* ${BACKEND_API_LEVEL}\t<= ${BACKEND_MANIFEST}"
  echo -e "* ${SERVER_API_LEVEL}\t<= x${SERVER_MANIFEST}"
  exit 1
fi

ANDROID_API_LEVEL=$SERVER_API_LEVEL

SERVER_MIN_VERSION=$(min_sdk "${SERVER_MANIFEST}")
BACKEND_MIN_VERSION=$(min_sdk "${BACKEND_MANIFEST}")

if [ "${SERVER_MIN_VERSION}" != "${BACKEND_MIN_VERSION}" ]; then
  error "Expected android:minSdkVersion to be the same in these files:"
  error "* ${BACKEND_MIN_VERSION}\t<= ${BACKEND_MANIFEST}"
  error "* ${SERVER_MIN_VERSION}\t<= ${SERVER_MANIFEST}"
  exit 1
fi

info "android:targetSdkVersion=${SERVER_API_LEVEL}"
info "   android:minSdkVersion=${SERVER_MIN_VERSION}"

banner "Expecting ANDROID env variables"

cd server/instrumentation-backend

if [ -z ${ANDROID_TOOLS_DIR+x} ]; then
  if [ -z ${ANDROID_HOME+x} ]; then
    error "\$ANDROID_HOME not set. Please specify an android home directory and \
    rerun:"
    error "ANDROID_HOME=/path/to/android_home ./build.sh"
    exit 1
  fi

  if [ -d "${ANDROID_HOME}/build-tools" ]; then
    first=$(ls -c1 "${ANDROID_HOME}/build-tools" | sort -r | head -n 1)
    export ANDROID_TOOLS_DIR="${ANDROID_HOME}/build-tools/${first}"
  elif [ -d "${ANDROID_HOME}/platform-tools" ]; then
    export ANDROID_TOOLS_DIR="${ANDROID_HOME}/platform-tools"
  else
    error "Unable to find android build tools in ${ANDROID_HOME}"
    error "For full instuctions see: https://github.com/calabash/calabash-androi\
    d/wiki/Building-calabash-android"
    exit 1
  fi
elif [ ! -d "${ANDROID_TOOLS_DIR}" ]; then
  error "Tools dir '${ANDROID_TOOLS_DIR}' does not exist"
  error "Please specify a valid tools dir and try again:"
  error "ANDROID_TOOLS_DIR=/path/to/tools_dir ./build.sh"
  exit 1
fi

CMD="./gradlew clean preparePackage \
  -Ptools_dir=${ANDROID_TOOLS_DIR} \
  -Pandroid_api_level=${ANDROID_API_LEVEL} \
  -Pversion=${CALABASH_ANDROID_SERVER_VERSION}"

shell "${CMD}"

$CMD

banner "Done"

info "Built server ${CALABASH_ANDROID_SERVER_VERSION} with command:"

shell "${CMD}"

info "using ${ANDROID_TOOLS_DIR}"

info "android:targetSdkVersion=${SERVER_API_LEVEL}"
info "   android:minSdkVersion=${SERVER_MIN_VERSION}"
