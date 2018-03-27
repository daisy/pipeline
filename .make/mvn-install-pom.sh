[[ -n ${VERBOSE+x} ]] && set -x
set -e
set -o pipefail
for arg in "$@"; do
    cd $arg
    eval $MVN org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -DpomFile=pom.xml -Dfile=./pom.xml | eval $MVN_LOG
done
