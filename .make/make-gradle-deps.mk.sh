#!/usr/bin/env bash
set -e

module=$1

v=$((cat $module/gradle.properties | grep '^distVersion' ||
     cat $module/gradle.properties | grep '^version' ) | sed 's/.*=//')
a=$(basename $module)
if [ -e $module/settings.gradle ] && cat $module/settings.gradle | grep "rootProject.name" >/dev/null; then
	a=$(cat $module/settings.gradle | grep '^rootProject.name' | sed "s/^rootProject\.name *= *['\"]\(.*\)['\"]/\1/")
fi
g=$(cat $module/build.gradle | grep '^group' | sed "s/^group *= *['\"]\(.*\)['\"]/\1/")
if [[ -z $g ]]; then
	g=$(cat $module/gradle.properties | grep '^group' | sed "s/^group *= *['\"]\(.*\)['\"]/\1/")
fi
echo "$module/VERSION := $v"
echo ""
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
if [ -e $module/integrationtest ]; then
	echo "$module/.test : \$(call rwildcard,$module/integrationtest/,*)"
fi
if [[ $v == *-SNAPSHOT ]]; then
	echo ""
	echo "\$(MVN_LOCAL_REPOSITORY)/$(echo $g |tr . /)/$a/$v/$a-$v.jar : $module/.install-jar"
	echo "	+\$(EVAL) 'test -e' \$@"
	echo "	+\$(EVAL) touch \$@"
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
	# FIXME: gradle eclipse does not link up projects
	# FIXME: gradle eclipse does not take into account localRepository from .gradle-settings/conf/settings.xml
	# when creating .classpath (but it does need the dependencies to be installed in .maven-workspace)
	echo "$module/.project : $module/build.gradle $module/gradle.properties $module/.dependencies .group-eval"
	echo "	+\$(EVAL) '${MY_DIR}/gradle-eclipse.sh' \$\$(dirname \$@)"
	echo ""
	echo "clean-eclipse : $module/.clean-eclipse"
	echo ".PHONY : $module/.clean-eclipse"
	echo "$module/.clean-eclipse :"
		echo "	if ! git ls-files --error-unmatch $module/.project >/dev/null 2>/dev/null; then \\"
		echo "		rm -rf \$(addprefix $module/,.project .classpath); \\"
		echo "	else \\"
		echo "		git checkout HEAD -- \$(addprefix $module/,.project .classpath); \\"
		echo "	fi"
fi
