#! /usr/bin/env bash

set -e

rm -rf "./tmp"
mkdir "./tmp"

rm -rf "./out"
mkdir "./out"

find ./src -name "*.java" | \
  javac -cp  "$ANDROID_HOME/platforms/android-24/android.jar:libs/build/Calabash.jar:libs/junit-4.12.jar" -d tmp @/dev/stdin

cd "tmp"
find . -name "*.class" | \
  jar cvf CalabashIntegrationTests.jar @/dev/stdin
cd ..
mv "tmp/CalabashIntegrationTests.jar" "out/CalabashIntegrationTests.jar"

rm -rf "./tmp"
