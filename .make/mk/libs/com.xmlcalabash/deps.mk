libs/com.xmlcalabash/VERSION := 1.1.20-p20-98-SNAPSHOT

$(TARGET_DIR)/state/libs/com.xmlcalabash/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : libs/com.xmlcalabash/.test
libs/com.xmlcalabash/.test : | .gradle-init .group-eval
	+$(EVAL) gradle.test("libs/com.xmlcalabash");

libs/com.xmlcalabash/.test : %/.test : %/build.gradle %/gradle.properties %/.dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/com.xmlcalabash/1.1.20-p20-98-SNAPSHOT/com.xmlcalabash-1.1.20-p20-98-SNAPSHOT.jar : libs/com.xmlcalabash/.install.jar
	+$(EVAL) exit(new File("$@").exists());
	+$(EVAL) touch("$@");

.SECONDARY : libs/com.xmlcalabash/.install.jar
libs/com.xmlcalabash/.install.jar : %/.install.jar : %/.install

.SECONDARY : libs/com.xmlcalabash/.install
libs/com.xmlcalabash/.install : | .gradle-init .group-eval
	+$(EVAL) gradle.install("libs/com.xmlcalabash");

libs/com.xmlcalabash/.install : %/.install : %/build.gradle %/gradle.properties %/.dependencies

.SECONDARY : libs/com.xmlcalabash/.dependencies
libs/com.xmlcalabash/.dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/com.xmlcalabash/1.1.20-p20-98/com.xmlcalabash-1.1.20-p20-98.jar : libs/com.xmlcalabash/.release

.SECONDARY : libs/com.xmlcalabash/.release
libs/com.xmlcalabash/.release : | .gradle-init .group-eval
	+$(EVAL) gradle.release("libs/com.xmlcalabash");

libs/com.xmlcalabash/.project : libs/com.xmlcalabash/build.gradle libs/com.xmlcalabash/gradle.properties libs/com.xmlcalabash/.dependencies .group-eval
	+$(EVAL) gradle.eclipse("libs/com.xmlcalabash");

clean-eclipse : libs/com.xmlcalabash/.clean-eclipse
.PHONY : libs/com.xmlcalabash/.clean-eclipse
libs/com.xmlcalabash/.clean-eclipse :
	$(call bash, \
		if ! git ls-files --error-unmatch libs/com.xmlcalabash/.project >/dev/null 2>/dev/null; then \
			rm -rf $(addprefix libs/com.xmlcalabash/,.project .classpath); \
		else \
			git checkout HEAD -- $(addprefix libs/com.xmlcalabash/,.project .classpath); \
		fi \
	)
