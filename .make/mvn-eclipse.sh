[[ -n ${VERBOSE+x} ]] && set -x
set -e
set -o pipefail
modules=( "$@" )
eval $MVN --projects $(printf '%s\n' "${modules[@]}" |paste -sd , -) \
          org.apache.maven.plugins:maven-eclipse-plugin:2.10:eclipse \
          -Declipse.useProjectReferences=true \
          -Declipse.addGroupIdToProjectName=true \
| eval $MVN_LOG

# import projects with eclim if available
if which eclim >/dev/null && eclim -command ping >/dev/null 2>/dev/null; then
    if [ "$CURDIR" == "$( eval printf "$(eclim -command workspace_dir)" )" ]; then
        for module in "$@"; do
            if [ -e "$CURDIR/$module/.project" ]; then
                msg=$( eclim -command project_import -f "$CURDIR/$module" )
                msg=$( eval printf "$msg" )
                if project=$( perl -e '$ARGV[0] =~ /^Project with name \x27(.+?)\x27 already exists/ and print "$1" or exit 1' "$msg" ); then
                    msg=$( eclim -command project_update -p $project )
                    eval printf "$msg"
                    echo
                else
                    echo "$msg"
                fi
            fi
        done
    fi
fi
