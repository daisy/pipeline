#!/usr/bin/env bash
[[ -n ${VERBOSE+x} ]] && set -x
set -e
set -o pipefail
shopt -s nocasematch

for arg in "$@"; do
    cd "$ROOT_DIR/$arg"
    $MVN clean install -Dmaven.test.skip -Dinvoker.skip=true
done

