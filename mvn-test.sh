[[ -n ${VERBOSE+x} ]] && set -x
set -e
cd $1
eval $MVN clean verify | eval $MVN_LOG
