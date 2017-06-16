[[ -n ${VERBOSE+x} ]] && set -x
set -e
cd $1
eval $MVN clean install -DskipTests -Dinvoker.skip=true | eval $MVN_LOG
