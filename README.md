| master  | develop |
|---------|---------|
|[![Build Status](http://calabash-ci.xyz:8081/job/calabash-android-server/job/master/badge/icon)](http://calabash-ci.xyz:8081/job/calabash-android-server/job/master/) | [![Build Status](http://calabash-ci.xyz:8081/job/calabash-android-server/job/develop/badge/icon)](http://calabash-ci.xyz:8081/job/calabash-android-server/job/develop/)
|[![Build Status](https://travis-ci.org/calabash/calabash-android-server.svg?branch=master)](https://travis-ci.org/calabash/calabash-android-server) | [![Build Status](https://travis-ci.org/calabash/calabash-android-server.svg?branch=develop)](https://travis-ci.org/calabash/calabash-android-server)

# calabash-android-server
The test-server for Calabash-Android

Automated Functional testing for Android based on cucumber http://calaba.sh 

### Building

Requirements:

- Java 8.
- Ruby >= 2.2. The latest ruby release is preferred.
- Android build-tools and Android Platform (will be installed by gradle).
- Android device/emulator and ADB for local testing.

```
$ git clone https://github.com/calabash/calabash-android-server.git
$ cd calabash-android-server/server
$ ./gradlew clean assembleAndroidTest
```

The final server apk file can be found in `calabash-android-server/server/app/build/outputs/apk/androidTest/debug/TestServer.apk` folder.

### Testing

Start Android device/emulator and make sure that device is visible via ADB before executing tests: 

```
adb devices
```

Execute test runner:

```
# Go to integration-tests folder
$ cd server/integration-tests

# Execute build for test project and tests
$ ./run_and_compile.sh
```

### Troubleshooting

If you have issues with build:
- Make sure that you have correct version of required dev tools/SDKs.

- Verify environment variables on your machine.

If you have issues with installing/running test apk:
- Make sure that device/emulator is visible via ADB.
- Verify that Android Platform 24 is installed properly. If not, install this manually.
- Enable ADB logs by modifying `run_and_compile.sh`:
```
# Disable "exit immediately" mode
# set -e

# ...

# Add this line to clear logs
adb logcat -c

# Install app and run tests
bundle exec calabash-android run unittest.apk

# Add this line to receive logs
adb logcat -d
```
