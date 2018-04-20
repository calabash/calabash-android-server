#!/usr/bin/env bash

bin/build.sh
bin/start_emulator.sh
cd server/integration-tests
./run_and_compile.sh