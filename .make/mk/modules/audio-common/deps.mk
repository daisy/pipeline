modules/audio-common/VERSION := 5.1.10-SNAPSHOT

$(TARGET_DIR)/state/modules/audio-common/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/audio-common/modified-since-release_ : modules/audio-common/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/audio-common/.test
modules/audio-common/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/audio-common/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/audio-common/5.1.10-SNAPSHOT/audio-common-5.1.10-SNAPSHOT.pom : modules/audio-common/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/audio-common/5.1.10-SNAPSHOT/audio-common-5.1.10-SNAPSHOT% : modules/audio-common/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/audio-common/.install.pom
modules/audio-common/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/audio-common");

modules/audio-common/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/audio-common/.install.jar
modules/audio-common/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/audio-common/.install
modules/audio-common/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/audio-common/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/audio-common/.install-doc.jar
modules/audio-common/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/audio-common/.install-xprocdoc.jar
modules/audio-common/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/audio-common/.install-javadoc.jar
modules/audio-common/.install-javadoc.jar : %/.install-javadoc.jar : %/.install-doc

.SECONDARY : modules/audio-common/.install-doc
modules/audio-common/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/audio-common/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/audio-common/.compile-dependencies modules/audio-common/.test-dependencies
modules/audio-common/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/audio-common/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/audio-common/5.1.10/audio-common-5.1.10.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/audio-common/5.1.10/audio-common-5.1.10-% : modules/audio-common/.release
	+//

.SECONDARY : modules/audio-common/.release
modules/audio-common/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("audio-common");

modules/audio-common/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/audio-common/.clean
.PHONY : modules/audio-common/.clean
modules/audio-common/.clean :
	rm("modules/audio-common/target");
