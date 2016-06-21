#! /usr/bin/env bash

./compile_calabash.sh
./compile_tests.sh
./build_test_apk.sh
./setup_unit_tests.sh

cd calabash-test-suite
bundle exec calabash-android run unittest.apk
