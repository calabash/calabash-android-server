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