#!/usr/bin/env bash

uname -a
$ANDROID_HOME/tools/bin/sdkmanager --version

cd $ANDROID_HOME/tools

echo "y" | bin/sdkmanager --update
echo "y" | bin/sdkmanager --install tools
echo "y" | bin/sdkmanager --install platform-tools
echo "y" | bin/sdkmanager --install emulator
echo "y" | bin/sdkmanager --install 'platforms;android-28'
echo "y" | bin/sdkmanager --install 'build-tools;28.0.3'
echo "y" | bin/sdkmanager --install 'system-images;android-28;google_apis;x86'
