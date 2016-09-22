#!/bin/bash

# run from whatever directory you'd like; will check all projects with a catalog.xml file under current directory
SHORT="no"
if [ "$1" = "short" ]; then
    SHORT="yes"
fi

function debug_echo() {
    if [ "$SHORT" = "no" ]; then
        echo "$1"
    fi
}
export -f debug_echo

SUMMARY_STATUS="ok"
for catalog in $(find -type f | grep -v "\s" | grep src/main/resources/META-INF/catalog.xml); do
    debug_echo ""
    resource_dir=$(cd `dirname $catalog` && cd .. && pwd)
    pom_dir="`echo $resource_dir | sed 's/\/src\/.*//'`"
    module_name="`echo $resource_dir | sed 's/\/src\/main\/.*//' | sed 's/.*\///'`"
    cat $catalog | sed ':a;N;$!ba;s/\n/ /g' | sed 's/</\n</g' | grep nextCatalog | sed 's/.*catalog="//' | sed 's/".*//' \
                 | sort > /tmp/check-dependencies.catalog
    HAS_DEPENDENCIES="`cat /tmp/check-dependencies.catalog | wc -l`"
    if [ "$HAS_DEPENDENCIES" = "0" ]; then
        echo "$module_name: ok"
        debug_echo "    - module has no dependencies in catalog.xml; no further checking will be performed"
        continue
    fi
    
    find $resource_dir -type f | grep src/main | grep "x[ps]l$" | xargs cat | sed ':a;N;$!ba;s/\n/ /g' | sed 's/</\n</g' \
                 | grep "http:" | grep "href=\"" | sed 's/.*href="http:\/*//' | sed 's/".*//' \
                 | sed 's/^\([^\/]*\.\)*\([^\.\/]\+\)\.\([^\.\/]\+\)\/\(.*\)\/[^\/]*$/\3:\2:\4/' | tr '/' ':' \
                 | sed 's/modules:\([^:]*\):.*/modules:\1/' \
                 | grep -v ":$module_name$" \
                 | grep -v "^com:xmlcalabash:extension:steps$" \
                 | grep -v ":$" | grep -v "^org:w3:" | grep -v "^org:idpf:epub:30:spec$" | grep -v xmlns | grep -v "^com:google:p:.*:wiki$" \
                 | sort | uniq | sort > /tmp/check-dependencies.code
    find $resource_dir -type f | grep src/test/java | xargs cat | grep '\(pipeline\|braille\)Module("' | sed 's/^ *//' | sed 's/").*//' \
                 | sed 's/brailleModule("/org:daisy:pipeline:modules:braille:/' \
                 | sed 's/pipelineModule("/org:daisy:pipeline:modules:/' \
                 | sort | uniq | sort > /tmp/check-dependencies.xproc-maven-plugin
    cat $pom_dir/pom.xml | sed ':a;N;$!ba;s/\n/ /g' | sed 's/<dependencyManagement.*dependencyManagement>//' | sed 's/<build.*build>//' \
                 | sed 's/.*<dependencies>//' | sed 's/dependencies>.*//' | sed 's/<dependency/\n<dependency/g' | grep "^.dependency" \
                 | grep -v "scope>test</scope" | sed 's/\(<artifactId>[^<]*<\/artifactId>\).*\(<groupId>[^<]*<\/groupId>\)/\2 \1/' \
                 | sed 's/.*<groupId>//' | sed 's/<\/groupId>.*<artifactId>/:/' | sed 's/<.*//' | sed 's/\./:/g' \
                 | sort | uniq | sort > /tmp/check-dependencies.pom-runtime-all
    cat /tmp/check-dependencies.pom-runtime-all | grep "pipeline:modules:" > /tmp/check-dependencies.pom-runtime-modules
    HAS_XPROCSPEC_TEST="`find $resource_dir -type f | grep src/test/java | wc -l`"
    STATUS="ok"
    if [ "`diff /tmp/check-dependencies.catalog /tmp/check-dependencies.code | wc -l`" -gt "0" ]; then
        STATUS="error with code"
    else
        if [ "`diff /tmp/check-dependencies.catalog /tmp/check-dependencies.pom-runtime-modules | wc -l`" -gt "0" ]; then
            STATUS="error with runtime dependencies"
        else
            if [ "$HAS_XPROCSPEC_TEST" -gt 0 ]; then
                if [ "`diff /tmp/check-dependencies.catalog /tmp/check-dependencies.xproc-maven-plugin | wc -l`" -gt "0" ]; then
                    STATUS="error with xproc-maven-plugin"
                fi
            fi
        fi
    fi
    echo "$module_name: $STATUS"
    debug_echo "    - $pom_dir/pom.xml"
    if [ "$SHORT" = "no" ]; then
        cat /tmp/check-dependencies.code | sed 's/^/    - depends on: /'
    fi
    diff /tmp/check-dependencies.catalog /tmp/check-dependencies.code | grep "[><]" | sed 's/>/    - in code but not catalog: /' | sed 's/</    - in catalog but not in code: /'
    diff /tmp/check-dependencies.catalog /tmp/check-dependencies.pom-runtime-modules | grep "[><]" | sed 's/>/    - as pom runtime dependency but not catalog: /' | sed 's/</    - in catalog but not as pom runtime dependency: /'
    if [ "$HAS_XPROCSPEC_TEST" -gt 0 ]; then
        diff /tmp/check-dependencies.catalog /tmp/check-dependencies.xproc-maven-plugin | grep "[><]" | sed 's/>/    - imported in xproc-maven-plugin java test but not catalog: /' | sed 's/</    - in catalog but not in xproc-maven-plugin java test: /'
    fi
    if [ "$STATUS" != "ok" ]; then
        SUMMARY_STATUS=$STATUS
    fi
done
debug_echo ""
echo "Summary: $SUMMARY_STATUS"
