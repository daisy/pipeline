set -e

module=$1

v=$((cat $module/gradle.properties | grep '^distVersion' ||
     cat $module/gradle.properties | grep '^version' ) | sed 's/.*=//')
a=$(basename $module)
g=$(cat $module/build.gradle | grep '^group' | sed "s/^group *= *['\"]\(.*\)['\"]/\1/")
echo "$module/.last-tested : %/.last-tested : %/.test | .group-eval"
echo "	+\$(EVAL) touch \$@"
echo ""
echo ".SECONDARY : $module/.test"
echo "$module/.test : | .gradle-init .group-eval"
echo "	+\$(EVAL) 'bash .make/gradle-test.sh' \$\$(dirname \$@)"
echo ""
echo "$module/.test : %/.test : %/build.gradle %/gradle.properties \$(call rwildcard,$module/src/,*) %/.dependencies"
if [ -e $module/test ]; then
	echo "$module/.test : \$(call rwildcard,$module/test/,*)"
fi
if [[ $v == *-SNAPSHOT ]]; then
	echo ""
	echo "\$(MVN_WORKSPACE)/$(echo $g |tr . /)/$a/$v/$a-$v.jar : $module/.install-jar"
	echo ""
	echo ".SECONDARY : $module/.install-jar"
	echo "$module/.install-jar : %/.install-jar : %/.install"
	echo ""
	echo ".SECONDARY : $module/.install"
	echo "$module/.install : | .gradle-init .group-eval"
	echo "	+\$(EVAL) 'bash .make/gradle-install.sh' \$\$(dirname \$@)"
	echo ""
	echo "$module/.install : %/.install : %/build.gradle %/gradle.properties \$(call rwildcard,$module/src/,*) %/.dependencies"
	echo ""
	echo ".SECONDARY : $module/.dependencies"
	echo "$module/.dependencies :"
fi
echo ""
echo "\$(MVN_WORKSPACE)/$(echo $g |tr . /)/$a/${v%-SNAPSHOT}/$a-${v%-SNAPSHOT}.jar : $module/.release"
echo ""
echo ".SECONDARY : $module/.release"
if [[ $v == *-SNAPSHOT ]]; then
	echo "$module/.release : | .gradle-init .group-eval"
	echo "	+\$(EVAL) \"bash .make/gradle-release.sh \$\$(dirname \$@)\""
else
	# already released, but empty rule is needed because jar might not be in .maven-workspace yet
	echo "$module/.release :"
fi
