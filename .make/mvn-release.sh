#!/usr/bin/env bash
[[ -n ${VERBOSE+x} ]] && set -x
set -e
set -o pipefail

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

select_modules_xsl=$ROOT_DIR/$MY_DIR/mvn-release-select-modules.xsl
update_external_versions_xsl=$ROOT_DIR/$MY_DIR/mvn-release-update-external-versions.xsl
update_internal_versions_xsl=$ROOT_DIR/$MY_DIR/mvn-release-update-internal-versions.xsl
workaround_bug_4979_xsl=$ROOT_DIR/$MY_DIR/mvn-release-workaround-bug-4979.xsl

gitrepo_dir=$release_dir
while ! [ -e $gitrepo_dir/.gitrepo ]; do
    gitrepo_dir=$(dirname $gitrepo_dir)
done
remote=$(git config --file $gitrepo_dir/.gitrepo --get subrepo.remote)
github_owner_repo=$(
    echo "$remote" | perl -e 'while (<>) {
                                $_ =~ /^(?:https?:\/\/github\.com\/|git\@github\.com:)([^\.\/]+)\/([^\.\/]+)(\.git)?$/
                                  or die "not a github remote: $_";
                                print "$1/$2\n"; }')
github_owner=$(echo $github_owner_repo | cut -d/ -f1)
github_repo=$(echo $github_owner_repo | cut -d/ -f2)

base_commit=$(.git-utils/git-subrepo-status --fetch --sha-only $gitrepo_dir | sed "s|^$gitrepo_dir @ ||")
on_remote_branch=$(git branch -r --contains $base_commit | sed -n "s|^  subrepo/$gitrepo_dir/\(.*\)\$|\1|p" | head -n1)
if [[ -z $on_remote_branch ]]; then
    echo "commit $base_commit is not on a remote branch" >&2
    exit 1
fi

echo
echo "# checkout"
echo
echo ": first cd to the $github_repo repo you want to release from && \\"
echo "git fetch $remote $on_remote_branch && \\"

pom=$ROOT_DIR/$release_dir/.effective-pom.xml
$MY_DIR/mvn --quiet --projects $release_dir help:effective-pom -Doutput=$pom
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
rm $pom
release_branch=release/$tag
echo -n "git checkout -b $release_branch $base_commit"

if [ $release_dir != $gitrepo_dir ]; then
    echo " && \\"
    echo -n "cd ${release_dir#${gitrepo_dir}/}"
fi
echo

echo
echo "# fix poms before release"
echo

# update versions of external dependencies in dependepencyManagement section
# other external dependencies are handled by maven-release-plugin.
echo "java -cp $SAXON \\
     net.sf.saxon.Transform \\
     -xsl:$update_external_versions_xsl \\
     -s:pom.xml \\
     OUTPUT_FILENAME=pom.xml.new \\
     >/dev/null && \\"
echo "find . -name pom.xml.new -execdir bash -c 'diff -Biw {} pom.xml >/dev/null && rm {} || mv {} pom.xml' \; && \\"
echo "git add -u && \\"
echo -n "( git commit -m 'Resolve snapshot dependencies (BOMs/parents)' >/dev/null || true )"

# disable modules that should not be released
if [ ${#modules[@]} -gt 0 ]; then
    echo " && \\"
    echo "java -cp $SAXON \\
     net.sf.saxon.Transform \\
     -xsl:$select_modules_xsl \\
     -s:pom.xml \\
     ENABLED_MODULES='${modules[@]}' \\
     OUTPUT_FILENAME=pom.xml.new \\
     >/dev/null && \\"
    echo "find . -name pom.xml.new -execdir bash -c 'diff -Biw {} pom.xml >/dev/null && rm {} || mv {} pom.xml' \; && \\"
    echo "git add -u && \\"
    echo -n "( git commit -m 'Disable modules that should not be released' >/dev/null || true )"
fi
echo

echo
echo "# release"
echo

echo ": add \"-DskipTests\" to -Darguments in order to skip the tests during release:prepare && \\"
if [ ${#modules[@]} -gt 0 ]; then
    
    # use temporary local repository for installing the artifacts during the prepare phase
    # which is needed for pax-exam based tests in a multi-module project (default preparation goal is verify)
    mkdir -p $tmp_dir
    
    # substitutions in settings file need to be made in advance because the release-plugin doesn't pass along
    # system properties to the sub-process (and we can't use -Darguments because of the Maven bug and we can't
    # apply the workaround below to the prepare step because the pom would get committed)
    # and also because Pax Exam wouldn't even resolve the system properties
    cat "$ROOT_DIR/$MY_DIR/mvn-release-settings.xml" | sed -e "s|\${releaseRepo}|$tmp_dir/repo|g" \
                                                           -e "s|\${cacheRepo}|$ROOT_DIR/$MVN_RELEASE_CACHE_REPO|g" \
                                                           -e "s|\${user\.home}|$HOME|g" \
                                                           >$tmp_dir/settings.xml
    # because we can't reliably use -Darguments
    # note however that below we pass -Darguments anyway because it might work (if the workaround is already
    # applied to the POM, or if no parent POMs define the arguments property)
    echo "env org.ops4j.pax.url.mvn.settings='$tmp_dir/settings.xml' \\"
fi
printf "mvn clean release:clean release:prepare \\
    -DupdateDependencies=false \\
    -DpushChanges=false"
if [[ -z $tag_format ]]; then
    printf " \\
    -DtagNameFormat=$tag"
fi
if [ ${#modules[@]} -gt 0 ]; then
    printf " \\
    -DpreparationGoals='clean install' \\
    -Darguments=\"-Ddocumentation -Dorg.ops4j.pax.url.mvn.settings='$tmp_dir/settings.xml'\" \\
    --settings '$tmp_dir/settings.xml'"
fi
echo " && \\"
echo "git diff-index --quiet HEAD"

# parents were updated by release-plugin but not the ones of disabled modules and also internal bom import is not updated
if [ ${#modules[@]} -gt 0 ]; then
    echo
    echo "# fix poms after release (pt. 1)"
    echo
    echo "java -cp $SAXON \\
     net.sf.saxon.Transform \\
     -xsl:$update_internal_versions_xsl \\
     -s:pom.xml \\
     OUTPUT_FILENAME=pom.xml.new \\
     >/dev/null && \\"
    echo "find . -name pom.xml.new -execdir bash -c 'diff -Biw {} pom.xml >/dev/null && rm {} || mv {} pom.xml' \; && \\"
    echo "git add -u && \\"
    echo "git commit --amend --no-edit"
fi

echo
echo "# stage artifacts"
echo

# work around a Maven bug: https://issues.apache.org/jira/browse/MNG-4979
# note that this workaround can not be applied to the prepare step because the pom would get committed
echo "java -cp $SAXON \\
     net.sf.saxon.Transform \\
     -xsl:$workaround_bug_4979_xsl \\
     -s:pom.xml \\
     >pom.xml.fixed && \\"
echo "mv pom.xml.fixed pom.xml && \\"

# skipping tests during perform: they were run during prepare step, no need to run them again
echo "mvn release:perform -DlocalCheckout=true \\
                    -Dgoals=\"verify \\
                             org.sonatype.plugins:nexus-staging-maven-plugin:1.6.8:deploy\" \\
                    -Darguments=\"-Psonatype-oss-release \\
                                 -Ddocumentation \\
                                 -DskipTests -Dinvoker.skip=true \\
                                 -DnexusUrl=https://oss.sonatype.org/ \\
                                 -DserverId=sonatype-nexus-staging \\
                                 -DstagingDescription='$release_dir $version' \\
                                 -DkeepStagingRepositoryOnCloseRuleFailure=true\" && \\"
echo "git checkout HEAD -- pom.xml"

if [ ${#modules[@]} -gt 0 ]; then
    echo
    echo "# fix poms after release (pt. 2)"
    echo
    echo "if [[ \$(git log -1 --pretty=format:%B HEAD~2) == 'Disable modules that should not be released' ]]; then \\"
    echo "    git revert --no-edit HEAD~2 && \\"
    echo "    git reset --soft HEAD^ && \\"
    echo "    git commit --amend --no-edit; \\"
    echo "fi"
fi

echo
echo "# push and make pull request"
echo
echo -n "git push -u $remote $release_branch:$release_branch"

# create PR
if [ $github_owner == "daisy" ]; then
    echo " && \\"
    if [ $release_dir != $gitrepo_dir ]; then
        title="Release ${release_dir#${gitrepo_dir}/} v$version"
    else
        title="Release v$version"
    fi
    if [ $release_dir != $gitrepo_dir ]; then
        nexus_staging_props_file="target/checkout/${release_dir#${gitrepo_dir}/}/target/nexus-staging/staging/*.properties"
    else
        nexus_staging_props_file="target/checkout/target/nexus-staging/staging/*.properties"
    fi
    echo "staging_repo_id=\$(cat $nexus_staging_props_file \\"
    echo "                  | grep 'stagingRepository.id' | sed 's/^stagingRepository\.id=//g') && \\"
    echo "credentials=\$( \\"
    
    # export GITHUB_USER=bertfrees
    # export GITHUB_ASK_PASS="pass github.com | head -1"
    
    if [[ -n ${GITHUB_USER+x} ]]; then
		:
	else
		echo "WARNING: GITHUB_USER variable not set" >&2
		echo "  echo -n \"Enter Github user name: \" >&2 && read user && \\"
	fi
    if [[ -n ${GITHUB_ASK_PASS+x} ]]; then
        echo -n "  pass=\$($GITHUB_ASK_PASS)"
    else
		echo "WARNING: GITHUB_ASK_PASS variable not set" >&2
        echo -n "  echo -n \"Enter password for Github user ${GITHUB_USER-"\$user"}: \" >&2 && read -s pass && echo \"***\" >&2"
    fi
    echo " && \\"
    echo "  echo \"${GITHUB_USER-"\$user"}:\$pass\" \\"
    echo ") && \\"
    echo "pr_number=\$("
    echo "    echo \"{\\\"title\\\": \\\"$title\\\", \\"
    echo "           \\\"body\\\":  \\\"staged: https://oss.sonatype.org/content/repositories/\$staging_repo_id\\\", \\"
    echo "           \\\"head\\\":  \\\"$release_branch\\\", \\"
    echo "           \\\"base\\\":  \\\"master\\\"}\" \\"
    echo "    | curl -u \"\$credentials\" -X POST --data @- \\"
    echo "           https://api.github.com/repos/$github_owner/$github_repo/pulls \\"
    echo "    | jq -r '.number' ) && \\"
    milestone=$(xmllint --xpath "/*/*[local-name()='version']/text()" assembly/pom.xml)
    milestone=${milestone%-SNAPSHOT}
    milestone_json=$(
        curl https://api.github.com/repos/$github_owner/$github_repo/milestones 2>/dev/null \
        | jq --arg title "v${milestone}" '.[] | select(.title == $title)' )
    if [ -z "$milestone_json" ]; then
        echo ": Milestone 'v${milestone}' does not exist. Create it:"
        echo "echo \"{\\\"title\\\": \\\"v${milestone}\\\", \\"
        echo "       \\\"state\\\": \\\"open\\\"}\" \\"
        echo "| curl -u \"\$credentials\" -X POST --data @- \\"
        echo "       https://api.github.com/repos/$github_owner/$github_repo/milestones"
        echo ": Now add the issue to the milestone"
    else
        milestone_nr=$( echo "$milestone_json" | jq -r '.number' )
        echo "echo \"{\\\"milestone\\\": \\\"${milestone_nr}\\\"}\" \\"
        echo "| curl -u \"\$credentials\" -X PATCH --data @- \\"
        echo "       https://api.github.com/repos/$github_owner/$github_repo/issues/\$pr_number"
    fi
    echo -n "open \"https://github.com/$github_owner/$github_repo/pull/\$pr_number\""
fi

if [ $release_dir == "assembly" ]; then
    echo " && \\"
    echo -n "./update_rd.sh $tag"
    if [ $github_owner == "daisy" ]; then
        echo " && \\"
        echo "pr_number=\$("
        echo "    echo \"{\\\"title\\\": \\\"Release descriptor $version\\\", \\"
        echo "           \\\"head\\\":  \\\"rd-$version\\\", \\"
        echo "           \\\"base\\\":  \\\"gh-pages\\\"}\" \\"
        echo "    | curl -u \"\$credentials\" -X POST --data @- \\"
        echo "           https://api.github.com/repos/$github_owner/$github_repo/pulls \\"
        echo "    | jq -r '.number' ) && \\"
        if [[ -n ${milestone_nr+x} ]]; then
            echo "echo \"{\\\"milestone\\\": \\\"${milestone_nr}\\\"}\" \\"
            echo "| curl -u \"\$credentials\" -X PATCH --data @- \\"
            echo "       https://api.github.com/repos/$github_owner/$github_repo/issues/\$pr_number"
        fi
        echo -n ": open \"https://github.com/$github_owner/$github_repo/pull/\$pr_number\""
    fi
fi
echo

echo
echo "# pull into super project"
echo
echo "cd $ROOT_DIR && \\"
echo "git fetch subrepo/$gitrepo_dir && \\"
echo ".git-utils/git-subrepo/lib/git-subrepo -Ff commit $gitrepo_dir subrepo/$gitrepo_dir/$release_branch^ && \\"
echo "git commit --amend -m \"git subrepo pull $gitrepo_dir ($tag)\" -m \"\$(git log -1 --pretty=format:%B HEAD | tail -n+2)\""

if [ -e "$tmp_dir" ]; then
    echo
    echo "# cleanup"
    echo
    echo "rm -r $tmp_dir"
fi

echo
echo ": exit"

set +e
exit 100
