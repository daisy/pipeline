#!/usr/bin/env bash
[[ -n ${VERBOSE+x} ]] && set -x
set -e
set -o pipefail
shopt -s nocasematch

for arg in "$@"; do
    cd "$ROOT_DIR/$arg"
    $MVN org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -DpomFile=pom.xml -Dfile=./pom.xml
done
