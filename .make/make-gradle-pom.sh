#!/usr/bin/env bash
set -e

echo "<projects>"
for module in "$@"; do
    v=$((cat $module/gradle.properties | grep '^distVersion' ||
         cat $module/gradle.properties | grep '^version' ) | sed 's/.*=//')
    a=$(basename $module)
    g=$(cat $module/build.gradle | grep '^group' | sed "s/^group *= *['\"]\(.*\)['\"]/\1/")
    echo "  <project xmlns=\"http://maven.apache.org/POM/4.0.0\">"
    echo "    <groupId>$g</groupId>"
    echo "    <artifactId>$a</artifactId>"
    echo "    <version>$v</version>"
    echo "  </project>"
done
echo "</projects>"
