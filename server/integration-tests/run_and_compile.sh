#! /usr/bin/env bash

set -e

# Switch Java to v8 in runtime from current context
. switch_java_version.sh

./compile_calabash.sh
./compile_tests.sh
./build_test_apk.sh
./setup_unit_tests.sh

(
  cd calabash-test-suite
  bundle exec calabash-android run unittest.apk
)
