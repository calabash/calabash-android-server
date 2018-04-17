#! /usr/bin/env bash

set -e

rm -rf "./tmp"
mkdir "./tmp"

rm -rf "./out"
mkdir "./out"

find ./src -name "*.java" | \
  JAVA_HOME=`/usr/libexec/java_home -v 1.8` \
  javac -cp  "$ANDROID_HOME/platforms/android-22/android.jar:libs/build/robotium-solo-4.3.1.jar:libs/build/InstrumentationExposed.jar:libs/build/maps.jar:libs/build/Calabash.jar:libs/junit-4.12.jar" -d tmp @/dev/stdin

cd "tmp"
find . -name "*.class" | \
  JAVA_HOME=`/usr/libexec/java_home -v 1.8` jar cvf CalabashIntegrationTests.jar @/dev/stdin
cd ..
mv "tmp/CalabashIntegrationTests.jar" "out/CalabashIntegrationTests.jar"

rm -rf "./tmp"
