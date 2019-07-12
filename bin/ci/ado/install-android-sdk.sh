#!/usr/bin/env bash

pushd "${ANDROID_HOME}/tools" > /dev/null
  bin/sdkmanager --version
  echo "y" | bin/sdkmanager --update

  echo "y" | \
  bin/sdkmanager --install tools platform-tools emulator 'platforms;android-28' \
    'build-tools;28.0.3' 'system-images;android-28;google_apis;x86'
popd > /dev/null
