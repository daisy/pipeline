#!/usr/bin/env bash
[[ -n ${VERBOSE+x} ]] && set -x
set -e
for arg in "$@"; do
    cd $arg
    eval $GRADLE --no-search-upward test
done
