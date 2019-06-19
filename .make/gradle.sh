#!/usr/bin/env bash
[[ -n ${VERBOSE+x} ]] && set -x
set -e
if [ -e ./gradlew ]; then
    GRADLE=./gradlew
else
    GRADLE=$ROOT_DIR/$MY_DIR/gradlew
fi
$GRADLE "$@"
