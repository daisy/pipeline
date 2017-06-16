[[ -n ${VERBOSE+x} ]] && set -x
set -e
cd $1
eval $GRADLE install
