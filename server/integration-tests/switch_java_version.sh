#! /usr/bin/env bash

if [ "$(uname -s)" == "Darwin" ]; then
  export JAVA_HOME=`/usr/libexec/java_home -v 1.8`
elif [ "$(uname -s)" == "Linux" ]; then
  export JAVA_HOME=/usr/lib/jvm/java-8-oracle/
fi

echo "Using $JAVA_HOME path for JAVA_HOME variable"