#!/usr/bin/env bash
[[ -n ${VERBOSE+x} ]] && set -x
set -e
set -o pipefail
shopt -s nocasematch

if [ "$1" == "--dry-run" ]; then
    shift
    if [[ "${HOST_PLATFORM}" == "batch" ]]; then
        for arg in "$@"; do
            echo pushd $arg
            echo mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -DpomFile=pom.xml -Dfile=./pom.xml
            echo popd
        done
    else
        exit 1
    fi
else
    if [[ -z ${HOST_PLATFORM} ]]; then
        for arg in "$@"; do
            cd "$ROOT_DIR/$arg"
            $MVN org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -DpomFile=pom.xml -Dfile=./pom.xml
        done
    else
        exit 1
    fi
fi
