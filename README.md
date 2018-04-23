| master  | develop |
|---------|---------|
|[![Build Status](http://calabash-ci.xyz:8081/job/Calabash%20android%20server%20master/badge/icon)](http://calabash-ci.xyz:8081/job/Calabash%20android%20server%20master/) | [![Build Status](http://calabash-ci.xyz:8081/job/Calabash%20android%20server%20develop/badge/icon)](http://calabash-ci.xyz:8081/job/Calabash%20android%20server%20develop/)
|[![Build Status](https://travis-ci.org/calabash/calabash-android-server.svg?branch=master)](https://travis-ci.org/calabash/calabash-android-server) | [![Build Status](https://travis-ci.org/calabash/calabash-android-server.svg?branch=develop)](https://travis-ci.org/calabash/calabash-android-server)

# calabash-android-server
The test-server for Calabash-Android

Automated Functional testing for Android based on cucumber http://calaba.sh 

### Building

Requirements:

- Java 8.
- Ruby >= 2.2. The latest ruby release is preferred.
- Android build-tools v26.0.2.
- Android Platform v22.
- Android device/emulator and ADB for local testing.

```
$ git clone https://github.com/calabash/calabash-android-server.git
$ cd calabash-android-server
$ bin/build.sh
```

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
- Enable ADB logs by modifying `run_and_compile.sh`:
```
# Disable "exit immediately" mode
# set -e

# ...

bundle install

# Add this line to clear logs
adb logcat -c

# Install app and run tests
bundle exec calabash-android run unittest.apk

# Add this line to receive logs
adb logcat -d
```
