#! /usr/bin/env bash

set -e

cp "unittest_new.apk" "calabash-test-suite-fast/unittest.apk"

(
  cd calabash-test-suite-fast
  bundle install
  bundle exec calabash-android resign unittest.apk
)
