#! /usr/bin/env bash

set -e

./run_and_compile.sh 2&>1 > /dev/null
cat result.log
