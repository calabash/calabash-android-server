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

$ANDROID_HOME/build-tools/26.0.2/dx --dex --output "1_Calabash.jar"  "../../../../libs/build/Calabash.jar"
$ANDROID_HOME/build-tools/26.0.2/dx --dex --output "2_hamcrest-core-1.3.jar"  "../../../../libs/hamcrest-core-1.3.jar"
$ANDROID_HOME/build-tools/26.0.2/dx --dex --output "3_junit-4.12.jar"  "../../../../libs/junit-4.12.jar"
$ANDROID_HOME/build-tools/26.0.2/dx --dex --output "4_CalabashIntegrationTests.jar"  "../../../../out/CalabashIntegrationTests.jar"
cd ..
cd ..
zip unittest_new.apk -r .
mv unittest_new.apk "../../unittest_new.apk"
cd ..
rm -rf "./tmp"
cd ..

