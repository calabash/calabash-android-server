#! /usr/bin/env bash

set -e

./fast.sh 2&>1 > /dev/null
cat result.log
