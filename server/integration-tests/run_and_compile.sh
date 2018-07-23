#! /usr/bin/env bash

set -e

# Switch Java to v8 in runtime from current context
. switch_java_version.sh

# Stage calabash-android sources, build the TestServer.apk,
# and install the new server in the calabash-android sources.
./install-latest-test-server.sh

# Create a .jar file from Calabash sources to be used to
# compile server unit tests.
./compile_calabash.sh

# Create a .jar file from integration test sources.
./compile_tests.sh

# Build unittester.apk
./build_test_apk.sh

# Synchronize manifest files and signatures between
# unittester.apk (AUT) and TestServer.apk
#
# Calls `bundle update`
./prepare-unittest-apk-for-install.sh

(
  cd calabash-test-suite
  SKIP_VERSION_CHECK=1 bundle exec calabash-android run \
    unittest.apk \
    --format pretty \
    --format junit --out test_report
)
