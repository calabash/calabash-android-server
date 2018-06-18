#! /usr/bin/env bash

set -e

mkdir -p "libs/build"
cd ..

mv app/build/intermediates/classes-jar/debug/classes.jar integration-tests/libs/build/Calabash.jar
cd "../integration-tests"
