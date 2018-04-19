#! /usr/bin/env bash

set -e

cd resources
rm -rf "./tmp"
mkdir "tmp"
cp "unittester.apk" "tmp"
cd "tmp"
unzip "unittester.apk"
rm "unittester.apk"
mkdir -p "assets"
cd "assets"
rm -rf "jars"
mkdir "jars"
cd "jars"
cp "../../../../libs/build/robotium-solo-4.3.1.jar" "1_robotium-solo-4.3.1.jar"
cp "../../../../libs/build/maps.jar" "2_maps.jar"
cp "../../../../libs/build/InstrumentationExposed.jar" "3_InstrumentationExposed.jar"
$ANDROID_HOME/build-tools/26.0.2/dx --dex --output "4_Calabash.jar"  "../../../../libs/build/Calabash.jar"
$ANDROID_HOME/build-tools/26.0.2/dx --dex --output "5_hamcrest-core-1.3.jar"  "../../../../libs/hamcrest-core-1.3.jar"
$ANDROID_HOME/build-tools/26.0.2/dx --dex --output "6_junit-4.12.jar"  "../../../../libs/junit-4.12.jar"
$ANDROID_HOME/build-tools/26.0.2/dx --dex --output "7_CalabashIntegrationTests.jar"  "../../../../out/CalabashIntegrationTests.jar"
cd ..
cd ..
zip unittest_new.apk -r .
mv unittest_new.apk "../../unittest_new.apk"
cd ..
rm -rf "./tmp"
cd ..

