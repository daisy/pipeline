#!/usr/bin/env bash
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
echo "	+\$(EVAL) '${MY_DIR}/gradle-test.sh' \$\$(dirname \$@)"
echo ""
echo "$module/.test : %/.test : %/build.gradle %/gradle.properties \$(call rwildcard,$module/src/,*) %/.dependencies"
if [ -e $module/test ]; then
	echo "$module/.test : \$(call rwildcard,$module/test/,*)"
fi
if [[ $v == *-SNAPSHOT ]]; then
	echo ""
	echo "\$(MVN_LOCAL_REPOSITORY)/$(echo $g |tr . /)/$a/$v/$a-$v.jar : $module/.install-jar"
	echo ""
	echo ".SECONDARY : $module/.install-jar"
	echo "$module/.install-jar : %/.install-jar : %/.install"
	echo ""
	echo ".SECONDARY : $module/.install"
	echo "$module/.install : | .gradle-init .group-eval"
	echo "	+\$(EVAL) ${MY_DIR}/gradle-install.sh \$\$(dirname \$@)"
	echo ""
	echo "$module/.install : %/.install : %/build.gradle %/gradle.properties \$(call rwildcard,$module/src/,*) %/.dependencies"
	echo ""
	echo ".SECONDARY : $module/.dependencies"
	echo "$module/.dependencies :"
fi
echo ""
echo "\$(MVN_LOCAL_REPOSITORY)/$(echo $g |tr . /)/$a/${v%-SNAPSHOT}/$a-${v%-SNAPSHOT}.jar : $module/.release"
echo ""
echo ".SECONDARY : $module/.release"
if [[ $v == *-SNAPSHOT ]]; then
	echo "$module/.release : | .gradle-init .group-eval"
	echo "	+\$(EVAL) \"${MY_DIR}/gradle-release.sh \$\$(dirname \$@)\""
else
	# already released, but empty rule is needed because jar might not be in .maven-workspace yet
	echo "$module/.release :"
fi
if [[ $v == *-SNAPSHOT ]]; then
	echo ""
	echo "$module/.project : $module/build.gradle $module/gradle.properties $module/.dependencies"
	echo "	cd \$(dir \$@) && \\"
	# FIXME: generates project name with "/" in it which Eclipse won't import
	echo "	gradle eclipse"
	echo ""
	echo "clean-eclipse : $module/.clean-eclipse"
	echo ".PHONY : $module/.clean-eclipse"
	echo "$module/.clean-eclipse :"
	if ! git ls-files --error-unmatch $module/.project >/dev/null 2>/dev/null; then
		echo "	rm -rf \$(addprefix $module/,.project .classpath)"
	else
		echo "	git checkout HEAD -- \$(addprefix $module/,.project .classpath)"
	fi
fi
