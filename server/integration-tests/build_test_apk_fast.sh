#! /usr/bin/env bash

JAVA_HOME=`/usr/libexec/java_home -v 1.6` $ANDROID_HOME/build-tools/23.0.1/dx --dex --output "4_Calabash.jar"  "libs/build/Calabash.jar"
JAVA_HOME=`/usr/libexec/java_home -v 1.6` $ANDROID_HOME/build-tools/23.0.1/dx --dex --output "7_CalabashIntegrationTests.jar"  "out/CalabashIntegrationTests.jar"
adb push 4_Calabash.jar /sdcard/jars/
adb push 7_CalabashIntegrationTests.jar /sdcard/jars/
rm 4_Calabash.jar
rm 7_CalabashIntegrationTests.jar
