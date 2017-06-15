set -e

module=$1

v=$((cat $module/gradle.properties | grep '^distVersion' ||
     cat $module/gradle.properties | grep '^version' ) | sed 's/.*=//')
a=$(basename $module)
g=$(cat $module/build.gradle | grep '^group' | sed "s/^group *= *['\"]\(.*\)['\"]/\1/")
while true; do
	echo "$module/.last-tested : %/.last-tested : %/.test"
	echo "ifndef DUMP_DEPENDENCIES"
	echo "	touch \$@"
	echo "endif"
	echo ""
	echo ".SECONDARY : $module/.test"
	echo "$module/.test : | \$(MVN_WORKSPACE) .gradle-settings/conf/settings.xml"
	echo "ifndef DUMP_DEPENDENCIES"
	echo "	cd \$(dir \$@) && \\"
	echo "	\$(GRADLE) test"
	echo "endif"
	echo ""
	echo "$module/.test : %/.test : %/build.gradle %/gradle.properties %/src %/.dependencies"
	if [ -e $module/test ]; then
		echo "$module/.test : %/.test : %/test"
	fi
	echo ""
	jar=".maven-workspace/$(echo $g |tr . /)/$a/$v/$a-$v.jar"
	echo "$jar : $module/.install-jar"
	echo ""
	echo ".SECONDARY : $module/.install-jar"
	echo "$module/.install-jar : | $module/.install"
	echo ""
	echo ".SECONDARY : $module/.install"
	echo "$module/.install : | \$(MVN_WORKSPACE) .gradle-settings/conf/settings.xml"
	echo "ifdef DUMP_DEPENDENCIES"
	echo "	+dirname \$@"
	echo "else"
	echo "	cd \$(dir \$@) && \\"
	echo "	\$(GRADLE) install"
	echo "endif"
	echo ""
	echo "$module/.install : %/.install : %/build.gradle %/gradle.properties %/src %/.dependencies"
	echo ""
	echo ".SECONDARY : $module/.dependencies"
	echo "$module/.dependencies :"
	break
done
