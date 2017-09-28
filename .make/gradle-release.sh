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
exit 100
