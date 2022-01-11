
| master  | develop |
|---------|---------|
|[![Build Status](https://calabash-ci.xyz/job/calabash-android-server/job/master/badge/icon)](https://calabash-ci.xyz/job/calabash-android-server/job/master/) | [![Build Status](https://calabash-ci.xyz/job/calabash-android-server/job/develop/badge/icon)](https://calabash-ci.xyz/job/calabash-android-server/job/develop/)
|[![Build Status](https://travis-ci.org/calabash/calabash-android-server.svg?branch=master)](https://travis-ci.org/calabash/calabash-android-server) | [![Build Status](https://travis-ci.org/calabash/calabash-android-server.svg?branch=develop)](https://travis-ci.org/calabash/calabash-android-server)

## calabash-android-server

The test-server for Calabash-Android

Automated Functional testing for Android based on cucumber

### Building

Requirements:

- Java 8.
- Ruby >= 2.3.*
- Android build-tools and Android Platform (will be installed by gradle).
- Android device/emulator and ADB for local testing.

```
$ git clone https://github.com/calabash/calabash-android-server.git
$ cd calabash-android-server/server
$ ./gradlew clean assembleAndroidTest
```

The gradle task will stage the TestServer.apk and the AndroidManifest.xml to the
root directory of this repository.

### Testing

Start Android device/emulator and make sure that device is visible via ADB before executing tests:

```
$ adb devices
```

Execute test runner:

```
$ cd server/integration-tests
$ ./test.sh
```

### Troubleshooting

If you have issues with build:
- Make sure that you have correct version of required dev tools/SDKs.
- Verify environment variables on your machine.

If you have issues with installing/running test apk:
- Make sure that device/emulator is visible via ADB.

Modify the server/integration-tests/04-run-tests.sh as follows to emit
adb logcat logs after the tests.

```
# Disable "exit immediately" mode
# set -e

# ...

# Add this line to clear device logs
adb logcat -c

(
  cd calabash-test-suite
  SKIP_VERSION_CHECK=1 bundle exec calabash-android run \
    unittest.apk \
    --format pretty \
    --format junit --out test_report
)

# Add this line to receive device logs
adb logcat -d
```
