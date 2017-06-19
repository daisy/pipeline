[[ -n ${VERBOSE+x} ]] && set -x
set -e
for arg in "$@"; do
    cd $arg
    eval $MVN clean install -DskipTests -Dinvoker.skip=true | eval $MVN_LOG
done
