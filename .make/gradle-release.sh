#!/usr/bin/env bash
[[ -n ${VERBOSE+x} ]] && set -x
set -e
set -o pipefail

if [ "$SKIP_RELEASE" = "true" ]; then
    exit 0
fi

make_cmd="make"
if [[ -n $MAKEFLAGS ]]; then
    make_cmd+=" "
    if ! [[ $MAKEFLAGS == -* ]]; then
        make_cmd+="-"
    fi
    make_cmd+="$MAKEFLAGS"
fi
make_cmd+=" $MAKECMDGOALS"
echo "Release $1 manually. Then, run \`$make_cmd' again to continue." >&2

if [ $1 == "libs/com.xmlcalabash" ]; then
    version=$(cat $1/gradle.properties | grep '^version' | sed 's/.*=//' | sed 's/-SNAPSHOT//')
    distVersion=$(cat $1/gradle.properties | grep '^distVersion' | sed 's/.*=//' | sed 's/-SNAPSHOT//')
    echo "* cd to $1"
    echo "* update version in gradle.properties to $version and distVersion to $distVersion"
    echo "* commit -m \"XML Calabash $version released\""
    echo "* gradle -u uploadArchives"
    echo "* login to https://oss.sonatype.org and close stage"
    echo "* checkout subrepo and cherry-pick commit"
    echo "* git tag -as -m \"XML Calabash $version\" $distVersion"
fi

exit 100
