#!/usr/bin/env bash
[[ -n ${VERBOSE+x} ]] && set -x
set -e
set -o pipefail

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
    echo ": cd to $1"
    echo ": update version in gradle.properties to $version and distVersion to $distVersion"
    echo "git commit -m \"XML Calabash $version released\""
    echo "$ROOT_DIR/$MY_DIR/gradlew uploadArchives"
    echo "open \"https://oss.sonatype.org\""
    echo ": login and close stage"
    echo ": checkout subrepo and cherry-pick commit"
    echo "git tag -as -m \"XML Calabash $version\" $distVersion"
elif [ $1 == "libs/jsass" ]; then
    remote=$(git config --file $1/.gitrepo --get subrepo.remote)
    github_repo=$(
        echo "$remote" | perl -e 'while (<>) {
                                    $_ =~ /^(?:https?:\/\/github\.com\/|git\@github\.com:)([^\.\/]+)\/([^\.\/]+)(\.git)?$/
                                      or die "not a github remote: $_";
                                    print "$2\n"; }')
    base_commit=$(.git-utils/git-subrepo-status --fetch --sha-only $1 | sed "s|^$1 @ ||")
    on_remote_branch=$(git branch -r --contains $base_commit | sed -n "s|^  subrepo/$1/\(.*\)\$|\1|p" | head -n1)
    if [[ -z $on_remote_branch ]]; then
        echo "commit $base_commit is not on a remote branch" >&2
        exit 1
    fi
    echo ": first cd to the $github_repo repo you want to release from && \\"
    echo "git fetch $remote $on_remote_branch && \\"
    version=$(cat $1/gradle.properties | grep '^version' | sed 's/.*=//' | sed 's/-SNAPSHOT//')
    echo "git checkout org.daisy.libs && git merge $base_commit && \\"
    echo "./gradlew clean release && \\"
    echo ": make sure you enter a SNAPSHOT version as the next version && \\"
    echo "git checkout \$(git describe --abbrev=0) && \\"
    echo "./gradlew clean uploadArchives && \\"
    echo ": go to https://oss.sonatype.org, login and close and release stage"
fi

exit 100
