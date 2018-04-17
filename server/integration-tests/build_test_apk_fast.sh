#! /usr/bin/env bash

set -e

mkdir "jars"
cd "jars"
JAVA_HOME=`/usr/libexec/java_home -v 1.8` $ANDROID_HOME/build-tools/26.0.2/dx --dex --output "4_Calabash.jar"  "../libs/build/Calabash.jar"
JAVA_HOME=`/usr/libexec/java_home -v 1.8` $ANDROID_HOME/build-tools/26.0.2/dx --dex --output "7_CalabashIntegrationTests.jar"  "../out/CalabashIntegrationTests.jar"
cd ..
adb push jars /sdcard/
rm -rf "./jars"
