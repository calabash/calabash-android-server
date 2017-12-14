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
