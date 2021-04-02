#!/usr/bin/env bash
[[ -n ${VERBOSE+x} ]] && set -x
set -e
set -o pipefail
for arg in "$@"; do
    cd "$ROOT_DIR/$arg"
    $MVN clean verify
done
