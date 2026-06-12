modules/tts-adapter-cereproc/VERSION := 1.1.11-SNAPSHOT

$(TARGET_DIR)/state/modules/tts-adapter-cereproc/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/tts-adapter-cereproc/modified-since-release_ : modules/tts-adapter-cereproc/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/tts-adapter-cereproc/.test
modules/tts-adapter-cereproc/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-cereproc/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-cereproc/1.1.11-SNAPSHOT/tts-adapter-cereproc-1.1.11-SNAPSHOT.pom : modules/tts-adapter-cereproc/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-cereproc/1.1.11-SNAPSHOT/tts-adapter-cereproc-1.1.11-SNAPSHOT% : modules/tts-adapter-cereproc/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/tts-adapter-cereproc/.install.pom
modules/tts-adapter-cereproc/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/tts-adapter-cereproc");

modules/tts-adapter-cereproc/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/tts-adapter-cereproc/.install.jar
modules/tts-adapter-cereproc/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/tts-adapter-cereproc/.install
modules/tts-adapter-cereproc/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-cereproc/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/tts-adapter-cereproc/.install-doc.jar
modules/tts-adapter-cereproc/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/tts-adapter-cereproc/.install-xprocdoc.jar
modules/tts-adapter-cereproc/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/tts-adapter-cereproc/.install-doc
modules/tts-adapter-cereproc/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-cereproc/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/tts-adapter-cereproc/.compile-dependencies modules/tts-adapter-cereproc/.test-dependencies
modules/tts-adapter-cereproc/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/tts-adapter-cereproc/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-cereproc/1.1.11/tts-adapter-cereproc-1.1.11.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-cereproc/1.1.11/tts-adapter-cereproc-1.1.11-% : modules/tts-adapter-cereproc/.release
	+//

.SECONDARY : modules/tts-adapter-cereproc/.release
modules/tts-adapter-cereproc/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("tts-adapter-cereproc");

modules/tts-adapter-cereproc/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/tts-adapter-cereproc/.clean
.PHONY : modules/tts-adapter-cereproc/.clean
modules/tts-adapter-cereproc/.clean :
	rm("modules/tts-adapter-cereproc/target");
