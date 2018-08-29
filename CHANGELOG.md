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
