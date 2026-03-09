libs/dotify.library/VERSION := 1.0.9-SNAPSHOT

$(TARGET_DIR)/state/libs/dotify.library/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : libs/dotify.library/.test
libs/dotify.library/.test : | .gradle-init .group-eval
	+$(EVAL) gradle.test("libs/dotify.library");

libs/dotify.library/.test : %/.test : %/build.gradle %/gradle.properties %/.dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/dotify/dotify.library/1.0.9-SNAPSHOT/dotify.library-1.0.9-SNAPSHOT.jar : libs/dotify.library/.install.jar
	+$(EVAL) exit(new File("$@").exists());
	+$(EVAL) touch("$@");

.SECONDARY : libs/dotify.library/.install.jar
libs/dotify.library/.install.jar : %/.install.jar : %/.install

.SECONDARY : libs/dotify.library/.install
libs/dotify.library/.install : | .gradle-init .group-eval
	+$(EVAL) gradle.install("libs/dotify.library");

libs/dotify.library/.install : %/.install : %/build.gradle %/gradle.properties %/.dependencies

.SECONDARY : libs/dotify.library/.dependencies
libs/dotify.library/.dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/dotify/dotify.library/1.0.9/dotify.library-1.0.9.jar : libs/dotify.library/.release

.SECONDARY : libs/dotify.library/.release
libs/dotify.library/.release : | .gradle-init .group-eval
	+$(EVAL) gradle.release("libs/dotify.library");

libs/dotify.library/.project : libs/dotify.library/build.gradle libs/dotify.library/gradle.properties libs/dotify.library/.dependencies .group-eval
	+$(EVAL) gradle.eclipse("libs/dotify.library");

clean-eclipse : libs/dotify.library/.clean-eclipse
.PHONY : libs/dotify.library/.clean-eclipse
libs/dotify.library/.clean-eclipse :
	$(call bash, \
		if ! git ls-files --error-unmatch libs/dotify.library/.project >/dev/null 2>/dev/null; then \
			rm -rf $(addprefix libs/dotify.library/,.project .classpath); \
		else \
			git checkout HEAD -- $(addprefix libs/dotify.library/,.project .classpath); \
		fi \
	)
