#! /usr/bin/env bash

set -e

mkdir "jars"
cd "jars"
$ANDROID_HOME/build-tools/26.0.2/dx --dex --output "1_Calabash.jar"  "../libs/build/Calabash.jar"
$ANDROID_HOME/build-tools/26.0.2/dx --dex --output "4_CalabashIntegrationTests.jar"  "../out/CalabashIntegrationTests.jar"
cd ..
adb push jars /sdcard/
rm -rf "./jars"
