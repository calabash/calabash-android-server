#!/usr/bin/env bash

CALABASH_ANDROID_SERVER_VERSION=$(cat version)
ANDROID_API_LEVEL=19

function verify_tool {
  command -v $1 > /dev/null
  if [ $? -ne 0 ]; then
    echo "Error: Command '${1}' not found"
    exit 1
  fi
}

cd server

verify_tool ant

if [ -z ${ANDROID_TOOLS_DIR+x} ]; then
  if [ -z ${ANDROID_HOME+x} ]; then
    echo "\$ANDROID_HOME not set. Please specify an android home directory and \
    rerun:"
    echo "ANDROID_HOME=/path/to/android_home ./build.sh"
    exit 2
  fi

  if [ -d "${ANDROID_HOME}/build-tools" ]; then
    first=$(ls -c1 "${ANDROID_HOME}/build-tools" | head -n 1)
    export ANDROID_TOOLS_DIR="${ANDROID_HOME}/build-tools/${first}"
  elif [ -d "${ANDROID_HOME}/platform-tools" ]; then
    export ANDROID_TOOLS_DIR="${ANDROID_HOME}/platform-tools"
  else
    echo "Unable to find android build tools in ${ANDROID_HOME}"
    echo "For full instuctions see: https://github.com/calabash/calabash-androi\
    d/wiki/Building-calabash-android"
    exit 4
  fi
elif [ ! -d "${ANDROID_TOOLS_DIR}" ]; then
  echo "Tools dir '${ANDROID_TOOLS_DIR}' does not exist"
  echo "Please specify a valid tools dir and try again:"
  echo "ANDROID_TOOLS_DIR=/path/to/tools_dir ./build.sh"
  exit 3
fi

CMD="ant clean package -debug -Dtools.dir=${ANDROID_TOOLS_DIR}\
  -Dandroid.api.level=${ANDROID_API_LEVEL} -Dversion=${CALABASH_ANDROID_SERVER_VERSION}"
echo "${CMD}"
$CMD
