[[ -n ${VERBOSE+x} ]] && set -x
set -e
for arg in "$@"; do
    cd $arg
    eval $GRADLE install
done
