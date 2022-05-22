#!/usr/bin/env bash
[[ -n ${VERBOSE+x} ]] && set -x
set -e
set -o pipefail
shopt -s nocasematch

if [ "$1" == "--dry-run" ]; then
    shift
    if [[ "${HOST_PLATFORM}" == "batch" ]]; then
        echo $MAKE -C assembly "$@"
    else
        exit 1
    fi
else
    if [[ -z ${HOST_PLATFORM} ]]; then
        # MAKECMDGOALS exported from main Makefile, but messes up recursive invocation of make
        unset MAKECMDGOALS
        eval $MAKE -C assembly "$@"
    else
        exit 1
    fi
fi
