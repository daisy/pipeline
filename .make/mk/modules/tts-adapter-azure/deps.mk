modules/tts-adapter-azure/VERSION := 1.1.6-SNAPSHOT

$(TARGET_DIR)/state/modules/tts-adapter-azure/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/tts-adapter-azure/modified-since-release_ : modules/tts-adapter-azure/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/tts-adapter-azure/.test
modules/tts-adapter-azure/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-azure/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-azure/1.1.6-SNAPSHOT/tts-adapter-azure-1.1.6-SNAPSHOT.pom : modules/tts-adapter-azure/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-azure/1.1.6-SNAPSHOT/tts-adapter-azure-1.1.6-SNAPSHOT% : modules/tts-adapter-azure/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/tts-adapter-azure/.install.pom
modules/tts-adapter-azure/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/tts-adapter-azure");

modules/tts-adapter-azure/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/tts-adapter-azure/.install.jar
modules/tts-adapter-azure/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/tts-adapter-azure/.install
modules/tts-adapter-azure/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-azure/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/tts-adapter-azure/.install-doc.jar
modules/tts-adapter-azure/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/tts-adapter-azure/.install-xprocdoc.jar
modules/tts-adapter-azure/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/tts-adapter-azure/.install-doc
modules/tts-adapter-azure/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-azure/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/tts-adapter-azure/.compile-dependencies modules/tts-adapter-azure/.test-dependencies
modules/tts-adapter-azure/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/tts-adapter-azure/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-azure/1.1.6/tts-adapter-azure-1.1.6.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-azure/1.1.6/tts-adapter-azure-1.1.6-% : modules/tts-adapter-azure/.release
	+//

.SECONDARY : modules/tts-adapter-azure/.release
modules/tts-adapter-azure/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("tts-adapter-azure");

modules/tts-adapter-azure/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/tts-adapter-azure/.clean
.PHONY : modules/tts-adapter-azure/.clean
modules/tts-adapter-azure/.clean :
	rm("modules/tts-adapter-azure/target");
