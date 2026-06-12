modules/tts-adapter-qfrency/VERSION := 1.0.15-SNAPSHOT

$(TARGET_DIR)/state/modules/tts-adapter-qfrency/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/tts-adapter-qfrency/modified-since-release_ : modules/tts-adapter-qfrency/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/tts-adapter-qfrency/.test
modules/tts-adapter-qfrency/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-qfrency/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-qfrency/1.0.15-SNAPSHOT/tts-adapter-qfrency-1.0.15-SNAPSHOT.pom : modules/tts-adapter-qfrency/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-qfrency/1.0.15-SNAPSHOT/tts-adapter-qfrency-1.0.15-SNAPSHOT% : modules/tts-adapter-qfrency/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/tts-adapter-qfrency/.install.pom
modules/tts-adapter-qfrency/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/tts-adapter-qfrency");

modules/tts-adapter-qfrency/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/tts-adapter-qfrency/.install.jar
modules/tts-adapter-qfrency/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/tts-adapter-qfrency/.install
modules/tts-adapter-qfrency/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-qfrency/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/tts-adapter-qfrency/.install-doc.jar
modules/tts-adapter-qfrency/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/tts-adapter-qfrency/.install-xprocdoc.jar
modules/tts-adapter-qfrency/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/tts-adapter-qfrency/.install-doc
modules/tts-adapter-qfrency/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-qfrency/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/tts-adapter-qfrency/.compile-dependencies modules/tts-adapter-qfrency/.test-dependencies
modules/tts-adapter-qfrency/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/tts-adapter-qfrency/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-qfrency/1.0.15/tts-adapter-qfrency-1.0.15.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-qfrency/1.0.15/tts-adapter-qfrency-1.0.15-% : modules/tts-adapter-qfrency/.release
	+//

.SECONDARY : modules/tts-adapter-qfrency/.release
modules/tts-adapter-qfrency/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("tts-adapter-qfrency");

modules/tts-adapter-qfrency/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/tts-adapter-qfrency/.clean
.PHONY : modules/tts-adapter-qfrency/.clean
modules/tts-adapter-qfrency/.clean :
	rm("modules/tts-adapter-qfrency/target");
