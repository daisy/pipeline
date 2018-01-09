#!/usr/bin/env bash
[[ -n ${VERBOSE+x} ]] && set -x
set -e
for arg in "$@"; do
    cd "$ROOT_DIR/$arg"
    eval $GRADLE --no-search-upward test
done
