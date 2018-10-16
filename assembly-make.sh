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
        # MVN_LOG from main.mk can not be passed on correctly to assembly Makefile
        export MVN_LOG="cat >> $ROOT_DIR/maven.log"
        # MAKECMDGOALS exported from main Makefile, but messes up recursive invocation of make
        unset MAKECMDGOALS
        # unconditionally make targets because this is what was decided by super-project based on the dependency analysis
        # ideally, the external dependency information should be passed to sub-project so that it can make the decision
        eval $MAKE -B -C assembly "$@"
    else
        exit 1
    fi
fi
