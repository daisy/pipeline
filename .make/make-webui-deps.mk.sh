set -e

echo ".SECONDARY : webui/.compile-dependencies"
echo -n "webui/.compile-dependencies :"
for a in clientlib-java clientlib-java-httpclient; do
    v=$(
        cat webui/build.sbt | grep "\"org.daisy.pipeline\" % \"$a\"" | sed "s/^.*\"$a\" % \"\(..*\)\".*/\1/"
    )
    if [[ $v == *-SNAPSHOT ]]; then
        printf " \\\\\n\t\$(MVN_WORKSPACE)/org/daisy/pipeline/$a/$v/$a-$v.jar"
    fi
done
echo ""
