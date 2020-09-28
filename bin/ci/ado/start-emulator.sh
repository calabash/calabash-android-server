#!/usr/bin/env bash
ANDROID_SDK_VERSION=29
echo "Ensuring no emulators are running"

$ANDROID_HOME/platform-tools/adb devices | \
  grep emulator | cut -f1 | \
  while read line; do adb -s $line emu kill || true; done

echo "Install Android SDK"
echo "y" | $ANDROID_HOME/tools/bin/sdkmanager --install 'system-images;android-'$ANDROID_SDK_VERSION';google_apis;x86'

echo "Creating emulator"
echo "no" | $ANDROID_HOME/tools/bin/avdmanager create avd \
  -n xamarin_android_emulator \
  -k 'system-images;android-'$ANDROID_SDK_VERSION';google_apis;x86' \
  --force

$ANDROID_HOME/emulator/emulator -list-avds

echo "Starting emulator"

nohup $ANDROID_HOME/emulator/emulator -avd xamarin_android_emulator \
  -no-snapshot > /dev/null 2>&1 &
$ANDROID_HOME/platform-tools/adb wait-for-device \
  shell 'while [[ -z $(getprop sys.boot_completed | tr -d '\r') ]]; do sleep 1; done; input keyevent 82'

echo "Disabling animations on emulator"

adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
