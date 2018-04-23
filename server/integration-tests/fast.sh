#! /usr/bin/env bash

set -e

# Switch Java to v8 in runtime from current context
. switch_java_version.sh

./compile_calabash.sh
./compile_tests.sh
./build_test_apk_fast.sh
./setup_unit_tests_fast.sh

cd "calabash-test-suite-fast"
bundle exec calabash-android run unittest.apk
