#!/usr/bin/env bash
[[ -n ${VERBOSE+x} ]] && set -x
set -e
for arg in "$@"; do
    cd "$ROOT_DIR/$arg"
    if cat build.gradle | grep "publishing {" >/dev/null; then
        eval $GRADLE publishToMavenLocal
    else
        eval $GRADLE install
    fi
done
