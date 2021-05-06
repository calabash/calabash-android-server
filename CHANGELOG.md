### 0.9.14

* Enable UIautomator2 changes #103

### 0.9.12

* location mocking: only stop mocking if we started #95

### 0.9.11

No changes to the server.  The purpose of this release is to sync with the
calabash-android gem version (to fix a bad release of the TestServer.apk).

The build system has been improved and the TestServer.apk and
AndroidManifest.xml are not validated as part of the build.

The TestServer.apk and AndroidManifest.xml are now in the git index.

### 0.9.10

* Do not trigger an "build for older version of Android" popup
* Fix location mocking on Android Q
* Fix entertext and friends to work on webview on newer version of Android (9+)

### 0.9.9

Register activity monitor earlier, so that all activies are guaranteed to be tracked. Fixes issue where drag coordinates would never return (stuck waiting for activity)

### 0.9.8

No behavior changes.  Advancing the version to release the
calabash-android gem.

### 0.9.7

* Support instrumentation registry #77
* Fix regression by changing maximum SDK version for ViewWrapper #79

### 0.9.6

* Test: integration tests use latest TestServer.apk #70
* Added workaround for Android 2.3.x: verify that the element isMarked
  without reflection #71
* Fix problem with backslash in query strings #72
* Fix wrong single quote replacement. #74

### 0.9.5

This release aligns the server and gem versions.

### 0.9.3

Added support for Android P (api 28) devices.

* Changed build process: added gradle build instead of ant.
* Removed the use of blacklisted api for latest android versions.
* Clean-up android server from external libs.

### 0.9.2

This version formalizes the targetSdkLevel and minSdkVersion properties
of the test server .apk.

```shell
targetSdkLevel="22"
 minSdkVersion="8"
```

* Align targetSdkVersion in manifests and build script #50

### 0.9.1

* Views without id should not generate log messages #46
* Travis: ensure ant is installed #45
* Fix exceptions when app is running with different locale than english #44
* Fix null pointer exception in getViews method #40
