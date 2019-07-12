#! /usr/bin/env bash

set -e

cp "unit-tester-new.apk" "calabash-test-suite/unit-tester.apk"

pushd calabash-test-suite > /dev/null
  bundle update
  bundle exec calabash-android resign unit-tester.apk
  bundle exec calabash-android build unit-tester.apk

  # The app will pop a sh.calaba.unittester "built for older version of Android"
  # dialog.  This can be ignored - it does not influence the success or failure
  # of the tests.
  SKIP_VERSION_CHECK=1 bundle exec calabash-android run \
    unit-tester.apk \
    --format pretty \
    --format junit \
    --out test-report
popd > /dev/null
