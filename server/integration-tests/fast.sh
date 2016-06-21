#! /usr/bin/env bash

./compile_calabash.sh
./compile_tests.sh
./build_test_apk_fast.sh

cd calabash-test-suite-fast
bundle exec calabash-android run unittest.apk
