#! /usr/bin/env bash

mv "unittest_new.apk" "calabash-test-suite/unittest.apk"
cd "calabash-test-suite"
bundle exec calabash-android resign unittest.apk
bundle exec calabash-android build unittest.apk
