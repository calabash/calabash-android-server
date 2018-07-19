#! /usr/bin/env bash

set -e

# Switch Java to v8 in runtime from current context
. switch_java_version.sh

# Stage calabash-android sources, build the TestServer.apk,
# and install the new server in the calabash-android sources.
./install-latest-test-server.sh

# ???
./compile_calabash.sh

# ???
./compile_tests.sh

# ???
./build_test_apk.sh

# ???
./setup_unit_tests.sh

(
  cd calabash-test-suite
  bundle update
  SKIP_VERSION_CHECK=1 bundle exec calabash-android run \
    unittest.apk \
    --format pretty \
    --format junit --out test_report
)
