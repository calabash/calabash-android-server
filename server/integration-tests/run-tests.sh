#! /usr/bin/env bash

set -e

source log.sh

# Switch Java to v8 in runtime from current context
if [ "$(uname -s)" == "Darwin" ]; then
  export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
elif [ "$(uname -s)" == "Linux" ]; then
  export JAVA_HOME=/usr/lib/jvm/java-8-oracle/
fi

info "Using $JAVA_HOME path for JAVA_HOME variable"

# Stage calabash-android sources, build the TestServer.apk,
# and install the new server in the calabash-android sources.
./00-install-latest-test-server.sh

# Create a .jar file from Calabash sources to be used to
# compile server unit tests.
./01-compile-calabash.sh

# Create a .jar file from integration test sources.
./02-compile-tests.sh

# Build unittester.apk
./03-build-test-apk.sh

# Synchronize manifest files and signatures between
# unittester.apk (AUT) and TestServer.apk
#
# Calls `bundle update`
./04-prepare-unittest-apk-for-install.sh

(
  cd calabash-test-suite
  SKIP_VERSION_CHECK=1 bundle exec calabash-android run \
    unittest.apk \
    --format pretty \
    --format junit --out test_report
)
