#! /usr/bin/env bash

set -e

mkdir -p "libs/build"
cd ..
cd "instrumentation-backend"
rm -rf "./tmp"
mkdir "./tmp"
find ./src -name "*.java" | \
  JAVA_HOME=`/usr/libexec/java_home -v 1.8` \
  javac -cp "$ANDROID_HOME/platforms/android-22/android.jar:libs/robotium-solo-4.3.1.jar:libs/InstrumentationExposed.jar:libs/maps.jar"  -d tmp @/dev/stdin
cd "tmp"
find . -name "*.class" | \
  JAVA_HOME=`/usr/libexec/java_home -v 1.8` jar cvf Calabash.jar @/dev/stdin
cd ..
mv tmp/Calabash.jar Calabash.jar
mv Calabash.jar "../integration-tests/libs/build"
rm -rf "./tmp"
cp "libs/robotium-solo-4.3.1.jar" "../integration-tests/libs/build/"
cp "libs/InstrumentationExposed.jar" "../integration-tests/libs/build/"
cp "libs/maps.jar" "../integration-tests/libs/build/"
cd "../integration-tests"
