set -e

# Get the current development version of each Maven module. If the
# module has not changed since the previous release, use the released
# version. If it only has changes in the POM, use the version from
# it's own dependency management.

# Then, for every Maven module check that its dependencies
# (incl. parent and dependencies from dependencyManagement) match the
# current development versions.

pom-diff() {
    pom_a=$1
    pom_b=$2
    java -cp $SAXON net.sf.saxon.Transform -xsl:.make/pom-diff.xsl -it:main pom_a=$pom_a pom_b=$pom_b 2>/dev/null
}

get-development-version() {
    local module groupId artifactId version
    module=$1
    groupId=$2
    artifactId=$3
    version=$(cat $module/pom.xml | bash .make/mvn-get-version.sh)
    [[ -n ${VERBOSE+x} ]] && echo "  version in pom: $version" >&2
    if [[ $version != *-SNAPSHOT ]]; then
        echo $version
    else
        local subrepo m
        m=$module
        while [[ -n $m ]]; do
            if [ -e $m/.gitrepo ]; then
                subrepo=$m
                break
            fi
            case $m in
                */*) m=${m%/*} ;;
                *)   m=        ;;
            esac
        done
        if [[ -z $subrepo ]]; then
            echo $version
        else
            local release_commit
            module=$module/
            module=${module#$subrepo/}
            # find latest release
            release_commit=$(
                .git-utils/git-subrepo-log $subrepo \
                    --filter "v=\$(git show \${GIT_COMMIT}:\${GIT_SUBTREE}${module}pom.xml | bash .make/mvn-get-version.sh) \
                              && [[ \$v != *-SNAPSHOT ]]" \
                    -n 1 \
                    ${module}pom.xml
            )
            if [[ -z $release_commit ]]; then
                echo $version
            else
                local current_tree release_tree
                [[ -n ${VERBOSE+x} ]] && echo "  release commit: $(echo $release_commit | cut -c1-7)" >&2
                current_tree=$subrepo/$module
                if git merge-base --is-ancestor $release_commit HEAD; then
                    release_tree=$release_commit:$subrepo/$module
                else
                    release_tree=$release_commit:$module
                fi
                release_version=$(git show ${release_tree}pom.xml | bash .make/mvn-get-version.sh)
                [[ -n ${VERBOSE+x} ]] && echo "  release version: $release_version" >&2
                # if there are differences in src/main return the snapshot version
                git diff --quiet ${release_tree}src/main HEAD:${current_tree}src/main 2>/dev/null
                if [[ $? != 0 && $? != 128 ]]; then
                    echo $version
                else
                    # if there are only white space changes, changes to the version number or changes to <scm/>, return
                    # the released version
                    if [[ $version != $release_version ]] \
                           && pom-diff <(git show ${release_tree}pom.xml) \
                                       <(cat ${current_tree}pom.xml); then
                        echo $release_version
                    else
                        # otherwise use the version from dependencyManagement; this version can be controlled via the bom
                        local bom_version
                        bom_version=$(
                            xmllint --xpath "/*/*[string(*[local-name()='groupId'])='$groupId' \
                                                  and string(*[local-name()='artifactId'])='$artifactId'] \
                                               /*[local-name()='dependencyManagement'] \
                                             /*/*[local-name()='dependency' \
                                                  and string(*[local-name()='groupId'])='$groupId' \
                                                  and string(*[local-name()='artifactId'])='$artifactId'][1] \
                                               /*[local-name()='version'] \
                                               /text()" \
                                    .effective-pom.xml 2>/dev/null
                        )
                        if [ $? == 0 ]; then
                            [[ -n ${VERBOSE+x} ]] && echo "  version in bom: $bom_version" >&2
                            echo $bom_version
                        else
                            # if dependencyManagement does not contain the artifact return either the released version
                            # or the snapshot version; in the majority of cases, pom changes are not relevant for the
                            # resulting artifact; in a few cases, which we hard-code, pom changes are relevant
                            case "$groupId:$artifactId" in
                                org.daisy.libs:jing)
                                    echo $version
                                    ;;
                                org.daisy.libs:saxon-he)
                                    echo $version
                                    ;;
                                org.daisy.pipeline.build:modules-test-helper)
                                    echo $version
                                    ;;
                                *-bom|*-parent)
                                    echo $version
                                    ;;
                                *)
                                    echo $release_version
                                    ;;
                            esac
                        fi
                    fi
                fi
            fi
        fi
    fi
}

while true; do
    echo "<dependencies xmlns=\"http://maven.apache.org/POM/4.0.0\">"
    cat .maven-modules | while read -r module; do
        groupId=$(xmllint --xpath "/*/*[local-name()='groupId']/text()" $module/pom.xml 2>/dev/null) || \
        groupId=$(xmllint --xpath "/*/*[local-name()='parent']/*[local-name()='groupId']/text()" $module/pom.xml)
        artifactId=$(xmllint --xpath "/*/*[local-name()='artifactId']/text()" $module/pom.xml)
        [[ -n ${VERBOSE+x} ]] && echo "$groupId:$artifactId" >&2
        version=$(get-development-version $module $groupId $artifactId)
        [[ -n ${VERBOSE+x} ]] && echo "  development version: $version" >&2
        echo "  <dependency>"
        echo "    <groupId>$groupId</groupId>"
        echo "    <artifactId>$artifactId</artifactId>"
        echo "    <version>$version</version>"
        echo "  </dependency>"
    done
    echo "</dependencies>"
    break
done | \
\
java -cp $CURDIR/$SAXON \
     net.sf.saxon.Transform \
     -xsl:.make/check-versions.xsl \
     -s:- \
     modules="$(cat .maven-modules)"
