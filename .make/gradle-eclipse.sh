#!/usr/bin/env bash
[[ -n ${VERBOSE+x} ]] && set -x
set -e
set -o pipefail

# start eclim if available
if which eclim >/dev/null; then
    if ! eclim -command ping >/dev/null 2>/dev/null; then
        echo "Please start Eclim" >&2
        exit 1
    fi
    if [ "$ROOT_DIR" != "$( eval printf "$(eclim -command workspace_dir)" )" ]; then
        echo "Please switch to your Eclipse workspace '$ROOT_DIR'" >&2
        exit 1
    fi
fi

for module in "$@"; do
    cd "$ROOT_DIR/$module"
    if ! grep -Fxq 'apply plugin: "eclipse"' build.gradle; then
        echo 'apply plugin: "eclipse"' > build.gradle.tmp
        cat build.gradle >> build.gradle.tmp
        mv build.gradle.tmp build.gradle
    fi
    eval $GRADLE eclipse
    # import project with eclim if available
    if which eclim >/dev/null; then
        if [ -e "$ROOT_DIR/$module/.project" ]; then
            msg=$( eclim -command project_import -f "$ROOT_DIR/$module" )
            # FIXME: printf '\u0027' does not work with older versions of bash
            msg=$( eval printf "$msg" )
            if project=$( perl -e '$ARGV[0] =~ /^Project with name \x27(.+?)\x27 already exists/ and print "$1" or exit 1' "$msg" ); then
                msg=$( eclim -command project_update -p $project )
                    eval printf "$msg"
                    echo
            else
                echo "$msg"
            fi
        fi
    else
        # print instructions if eclim not available
        echo "Now import the project at '$module' into your Eclipse workspace" >&2
    fi
done

