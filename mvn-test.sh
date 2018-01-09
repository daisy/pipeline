#!/usr/bin/env bash
[[ -n ${VERBOSE+x} ]] && set -x
set -e
set -o pipefail
for arg in "$@"; do
    cd "$ROOT_DIR/$arg"
    eval $MVN clean verify | eval $MVN_LOG
done
