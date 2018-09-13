#! /usr/bin/env bash

set -e

cp "unittest_new.apk" "calabash-test-suite/unittest.apk"
cd calabash-test-suite
bundle update
bundle exec calabash-android resign unittest.apk
bundle exec calabash-android build unittest.apk
