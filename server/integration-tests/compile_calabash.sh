#! /usr/bin/env bash

mkdir -p "libs/build"
cd ..
cd "instrumentation-backend"
rm -rf "./tmp"
mkdir "./tmp"
find ./src -name "*.java" | JAVA_HOME=`/usr/libexec/java_home -v 1.6` javac -cp "$ANDROID_HOME/platforms/android-19/android.jar:libs/robotium-solo-4.3.1.jar:libs/InstrumentationExposed.jar:libs/maps.jar" -d tmp @/dev/stdin
cd "tmp"
find . -name "*.class" | JAVA_HOME=`/usr/libexec/java_home -v 1.6` jar cvf Calabash.jar @/dev/stdin
cd ..
mv tmp/Calabash.jar Calabash.jar
#JAVA_HOME=`/usr/libexec/java_home -v 1.6` $ANDROID_HOME/build-tools/23.0.1/dx --dex --output CalabashDex.jar Calabash.jar
#mv CalabashDex.jar "test/libs/build"
mv Calabash.jar "../integration-tests/libs/build"
#rm Calabash.jar
rm -rf "./tmp"
cp "libs/robotium-solo-4.3.1.jar" "../integration-tests/libs/build/"
cp "libs/InstrumentationExposed.jar" "../integration-tests/libs/build/"
cp "libs/maps.jar" "../integration-tests/libs/build/"
cd "../integration-tests"
#javac -cp "Calabash.jar:/Users/tobias/android/android-sdk-macosx/platforms/android-19/android.jar:libs/robotium-solo-4.3.1.jar:libs/InstrumentationExposed.jar:libs/maps.jar" test/**/*.java
#cd "test"
#java -cp "Calabash.jar:/Users/tobias/android/android-sdk-macosx/platforms/android-19/android.jar:libs/robotium-solo-4.3.1.jar:libs/InstrumentationExposed.jar:libs/maps.jar" sh.calaba.json.IntentTest
