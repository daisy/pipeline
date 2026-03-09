libs/jsass/VERSION := 5.11.1-p1-SNAPSHOT

$(TARGET_DIR)/state/libs/jsass/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : libs/jsass/.test
libs/jsass/.test : | .gradle-init .group-eval
	+$(EVAL) gradle.test("libs/jsass");

libs/jsass/.test : %/.test : %/build.gradle %/gradle.properties %/.dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/io.bit3.jsass/5.11.1-p1-SNAPSHOT/io.bit3.jsass-5.11.1-p1-SNAPSHOT.jar : libs/jsass/.install.jar
	+$(EVAL) exit(new File("$@").exists());
	+$(EVAL) touch("$@");

.SECONDARY : libs/jsass/.install.jar
libs/jsass/.install.jar : %/.install.jar : %/.install

.SECONDARY : libs/jsass/.install
libs/jsass/.install : | .gradle-init .group-eval
	+$(EVAL) gradle.install("libs/jsass");

libs/jsass/.install : %/.install : %/build.gradle %/gradle.properties %/.dependencies

.SECONDARY : libs/jsass/.dependencies
libs/jsass/.dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/io.bit3.jsass/5.11.1-p1/io.bit3.jsass-5.11.1-p1.jar : libs/jsass/.release

.SECONDARY : libs/jsass/.release
libs/jsass/.release : | .gradle-init .group-eval
	+$(EVAL) gradle.release("libs/jsass");

libs/jsass/.project : libs/jsass/build.gradle libs/jsass/gradle.properties libs/jsass/.dependencies .group-eval
	+$(EVAL) gradle.eclipse("libs/jsass");

clean-eclipse : libs/jsass/.clean-eclipse
.PHONY : libs/jsass/.clean-eclipse
libs/jsass/.clean-eclipse :
	$(call bash, \
		if ! git ls-files --error-unmatch libs/jsass/.project >/dev/null 2>/dev/null; then \
			rm -rf $(addprefix libs/jsass/,.project .classpath); \
		else \
			git checkout HEAD -- $(addprefix libs/jsass/,.project .classpath); \
		fi \
	)
