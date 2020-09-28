#!/usr/bin/env bash
ANDROID_SDK_VERSION=29
pushd "${ANDROID_HOME}/tools" > /dev/null
  bin/sdkmanager --version
  echo "y" | bin/sdkmanager --update

  echo "y" | \
  bin/sdkmanager --install tools platform-tools emulator 'platforms;android-'$ANDROID_SDK_VERSION \
    'build-tools;28.0.3' 'system-images;android-'$ANDROID_SDK_VERSION';google_apis;x86'
popd > /dev/null
