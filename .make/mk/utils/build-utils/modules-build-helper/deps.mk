utils/build-utils/modules-build-helper/VERSION := 3.0.2-SNAPSHOT

$(TARGET_DIR)/state/utils/build-utils/modules-build-helper/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/utils/build-utils/modules-build-helper/modified-since-release_ : utils/build-utils/modules-build-helper/pom.xml $(TARGET_DIR)/state/utils/build-utils/modules-test-helper/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : utils/build-utils/modules-build-helper/.test
utils/build-utils/modules-build-helper/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

utils/build-utils/modules-build-helper/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-build-helper/3.0.2-SNAPSHOT/modules-build-helper-3.0.2-SNAPSHOT.pom : utils/build-utils/modules-build-helper/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-build-helper/3.0.2-SNAPSHOT/modules-build-helper-3.0.2-SNAPSHOT% : utils/build-utils/modules-build-helper/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : utils/build-utils/modules-build-helper/.install.pom
utils/build-utils/modules-build-helper/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("utils/build-utils/modules-build-helper");

utils/build-utils/modules-build-helper/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/build-utils/modules-build-helper/.install.jar
utils/build-utils/modules-build-helper/.install.jar : %/.install.jar : %/.install

.SECONDARY : utils/build-utils/modules-build-helper/.install
utils/build-utils/modules-build-helper/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

utils/build-utils/modules-build-helper/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/build-utils/modules-build-helper/.install-doc
utils/build-utils/modules-build-helper/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

utils/build-utils/modules-build-helper/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : utils/build-utils/modules-build-helper/.compile-dependencies utils/build-utils/modules-build-helper/.test-dependencies
utils/build-utils/modules-build-helper/.compile-dependencies :
utils/build-utils/modules-build-helper/.test-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-test-helper/3.0.0-SNAPSHOT/modules-test-helper-3.0.0-SNAPSHOT.jar

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-build-helper/3.0.2/modules-build-helper-3.0.2.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-build-helper/3.0.2/modules-build-helper-3.0.2-% : utils/build-utils/modules-build-helper/.release
	+//

.SECONDARY : utils/build-utils/modules-build-helper/.release
utils/build-utils/modules-build-helper/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

utils/build-utils/modules-build-helper/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-test-helper/3.0.0/modules-test-helper-3.0.0.jar

clean : utils/build-utils/modules-build-helper/.clean
.PHONY : utils/build-utils/modules-build-helper/.clean
utils/build-utils/modules-build-helper/.clean :
	rm("utils/build-utils/modules-build-helper/target");
