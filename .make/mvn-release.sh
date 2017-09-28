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
echo "Please run the following commands manually. Then, run \`$make_cmd' again to continue." >&2
echo

tmp_dir=$(mktemp -d 2>/dev/null || mktemp -d -t 'mvn-release')
rm -rf $tmp_dir

release_dir=$1
shift
modules=( "$@" )

select_modules_xsl=$CURDIR/.make/mvn-release-select-modules.xsl
update_versions_xsl=$CURDIR/.make/mvn-release-update-versions.xsl
workaround_bug_4979_xsl=$CURDIR/.make/mvn-release-workaround-bug-4979.xsl

gitrepo_dir=$release_dir
while ! [ -e $gitrepo_dir/.gitrepo ]; do
    gitrepo_dir=$(dirname $gitrepo_dir)
done
remote=$(git config --file $gitrepo_dir/.gitrepo --get subrepo.remote)
github_user_repo=$(
    echo "$remote" | perl -e 'while (<>) {
                                $_ =~ /^(?:https?:\/\/github\.com\/|git\@github\.com:)([^\.\/]+)\/([^\.\/]+)(\.git)?$/
                                  or die "not a github remote: $_";
                                print "$1/$2\n"; }')
github_user=$(echo $github_user_repo | cut -d/ -f1)
github_repo=$(echo $github_user_repo | cut -d/ -f2)

base_commit=$(.git-utils/git-subrepo-status --fetch --sha-only $gitrepo_dir | sed "s|^$gitrepo_dir @ ||")
on_remote_branch=$(git branch -r --contains $base_commit | sed -n "s|^  subrepo/$gitrepo_dir/\(.*\)\$|\1|p" | head -n1)
if [[ -z $on_remote_branch ]]; then
    echo "commit $base_commit is not on a remote branch" >&2
    exit 1
fi

echo ": first cd to the $github_repo repo you want to release from && \\"
echo "git fetch $remote $on_remote_branch && \\"

pom=$CURDIR/$release_dir/.effective-pom.xml
eval $MVN --quiet --projects $release_dir help:effective-pom -Doutput=$pom
artifactId=$(xmllint --xpath "/*/*[local-name()='artifactId']/text()" $pom)
groupId=$(xmllint --xpath "/*/*[local-name()='groupId']/text()" $pom 2>/dev/null) || \
groupId=$(xmllint --xpath "/*/*[local-name()='parent']/*[local-name()='groupId']/text()" $pom)
version=$(xmllint --xpath "/*/*[local-name()='version']/text()" $pom)
version=${version%-SNAPSHOT}
if tag_format=$(
       xmllint --xpath "/*/*[local-name()='build']
                          /*[local-name()='plugins']
                          /*[local-name()='plugin' and string(*[local-name()='artifactId'])='maven-release-plugin']
                          /*[local-name()='configuration']
                          /*[local-name()='tagNameFormat']/text()" $pom 2>/dev/null) || \
   tag_format=$(
       xmllint --xpath "/*/*[local-name()='build']
                          /*[local-name()='pluginManagement']
                          /*[local-name()='plugins']
                          /*[local-name()='plugin' and string(*[local-name()='artifactId'])='maven-release-plugin']
                          /*[local-name()='configuration']
                          /*[local-name()='tagNameFormat']/text()" $pom 2>/dev/null); then
    tag=$(echo "$tag_format" | sed -e "s/@{project\.artifactId}/$artifactId/" -e "s/@{project\.version}/$version/")
else
    echo ": tagNameFormat not defined in pom. Please set the TAG variable manually. For example: && \\"
    echo "TAG=$artifactId-$version && \\"
    echo ": some previous tags: \\"
    printf ': %s \\\n' $(git ls-remote --tags $remote | awk '{print $2}' | cut -d '/' -f 3 | grep -vF '^{}' | head -n4)
    echo "&& \\"
    tag="\${TAG}"
fi
release_branch=release/$tag
echo "git checkout -b $release_branch $base_commit && \\"

if [ $release_dir != $gitrepo_dir ]; then
    echo "cd ${release_dir#${gitrepo_dir}/} && \\"
fi

if [ ${#modules[@]} -gt 0 ]; then
    echo "java -cp $CURDIR/$SAXON \\
     net.sf.saxon.Transform \\
     -xsl:$select_modules_xsl \\
     -s:pom.xml \\
     ENABLED_MODULES='${modules[@]}' \\
     OUTPUT_FILENAME=pom.xml.new \\
     >/dev/null && \\"
    echo "find . -name pom.xml.new -execdir bash -c 'diff -Bw {} pom.xml >/dev/null && rm {} || mv {} pom.xml' \; && \\"
    echo "git add -u && \\"
    echo "( git commit -m 'Disable modules that should not be released' >/dev/null || true ) && \\"
fi

if [ ${#modules[@]} -gt 0 ]; then
    
    # use temporary local repository for installing the artifacts during the prepare phase
    # which is needed for pax-exam based tests in a multi-module project (default preparation goal is verify)
    mkdir -p $tmp_dir
    
    # substitutions in settings file need to be made in advance because the release-plugin doesn't pass along
    # system properties to the sub-process (and we can't use the "arguments" property, see below)
    # and also because Pax Exam wouldn't even resolve the system properties
    cat "$CURDIR/.make/mvn-release-settings.xml" | sed -e "s|\${releaseRepo}|$tmp_dir/repo|g" \
                                                       -e "s|\${cacheRepo}|$CURDIR/$MVN_CACHE|g" \
                                                       -e "s|\${user\.home}|$HOME|g" \
                                                       >$tmp_dir/settings.xml
    echo "env org.ops4j.pax.url.mvn.settings='$tmp_dir/settings.xml' \\"
fi
printf "mvn clean release:clean release:prepare \\
    -DupdateDependencies=false"
if [[ -z $tag_format ]]; then
    printf " \\
    -DtagNameFormat=$tag"
fi
if [ ${#modules[@]} -gt 0 ]; then
    printf " \\
    -DpreparationGoals='clean install' \\
    --settings '$tmp_dir/settings.xml'"
fi
echo " && \\"

echo "git diff-index --quiet HEAD && \\"

# parents were updated by release-plugin but not the ones of disabled modules and also bom import is not updated
if [ ${#modules[@]} -gt 0 ]; then
    echo "java -cp $CURDIR/$SAXON \\
     net.sf.saxon.Transform \\
     -xsl:$update_versions_xsl \\
     -s:pom.xml \\
     OUTPUT_FILENAME=pom.xml.new \\
     >/dev/null && \\"
    echo "find . -name pom.xml.new -execdir bash -c 'diff -Bw {} pom.xml >/dev/null && rm {} || mv {} pom.xml' \; && \\"
    echo "git add -u && \\"
    echo "git commit --amend --no-edit && \\"
fi

# work around a Maven bug: https://issues.apache.org/jira/browse/MNG-4979
# note that this workaround can not be applied to the prepare step because the pom would get committed
echo "java -cp $CURDIR/$SAXON \\
     net.sf.saxon.Transform \\
     -xsl:$workaround_bug_4979_xsl \\
     -s:pom.xml \\
     >pom.xml.fixed && \\"
echo "mv pom.xml.fixed pom.xml && \\"

# skipping tests during perform: they were run during prepare step, no need to run them again
echo "mvn release:perform -Dgoals=\"verify \\
                             org.sonatype.plugins:nexus-staging-maven-plugin:1.6.8:deploy\" \\
                    -Darguments=\"-Psonatype-oss-release \\
                                 -DskipTests -Dinvoker.skip=true \\
                                 -DnexusUrl=https://oss.sonatype.org/ \\
                                 -DserverId=sonatype-nexus-staging \\
                                 -DstagingDescription='$release_dir $version' \\
                                 -DkeepStagingRepositoryOnCloseRuleFailure=true\" && \\"
echo "git checkout HEAD -- pom.xml && \\"

if [ ${#modules[@]} -gt 0 ]; then
    echo "if [[ \$(git log -1 --pretty=format:%B HEAD~2) == 'Disable modules that should not be released' ]]; then \\"
    echo "    git revert --no-edit HEAD~2 && \\"
    echo "    git reset --soft HEAD^ && \\"
    echo "    git commit --amend --no-edit; \\"
    echo "fi && \\"
fi

echo "git push -u $remote $release_branch:$release_branch && \\"

echo "cd $CURDIR && \\"
echo "git fetch subrepo/$gitrepo_dir && \\"

echo "git subrepo commit $gitrepo_dir subrepo/$gitrepo_dir/$release_branch^ && \\"
echo "git commit --amend -m \"git subrepo pull $gitrepo_dir ($tag)\" -m \"\$(git log -1 --pretty=format:%B HEAD | tail -n+2)\" && \\"

if [ -e "$tmp_dir" ]; then
    echo "rm -r $tmp_dir"
fi

echo ": exit"

set +e
exit 100
