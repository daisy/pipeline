modules/tts-adapter-acapela/VERSION := 3.1.10-SNAPSHOT

$(TARGET_DIR)/state/modules/tts-adapter-acapela/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/tts-adapter-acapela/modified-since-release_ : modules/tts-adapter-acapela/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/tts-adapter-acapela/.test
modules/tts-adapter-acapela/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-acapela/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-acapela/3.1.10-SNAPSHOT/tts-adapter-acapela-3.1.10-SNAPSHOT.pom : modules/tts-adapter-acapela/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-acapela/3.1.10-SNAPSHOT/tts-adapter-acapela-3.1.10-SNAPSHOT% : modules/tts-adapter-acapela/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/tts-adapter-acapela/.install.pom
modules/tts-adapter-acapela/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/tts-adapter-acapela");

modules/tts-adapter-acapela/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/tts-adapter-acapela/.install.jar
modules/tts-adapter-acapela/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/tts-adapter-acapela/.install
modules/tts-adapter-acapela/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-acapela/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/tts-adapter-acapela/.install-doc.jar
modules/tts-adapter-acapela/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/tts-adapter-acapela/.install-xprocdoc.jar
modules/tts-adapter-acapela/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/tts-adapter-acapela/.install-doc
modules/tts-adapter-acapela/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-acapela/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/tts-adapter-acapela/.compile-dependencies modules/tts-adapter-acapela/.test-dependencies
modules/tts-adapter-acapela/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/tts-adapter-acapela/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-acapela/3.1.10/tts-adapter-acapela-3.1.10.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-acapela/3.1.10/tts-adapter-acapela-3.1.10-% : modules/tts-adapter-acapela/.release
	+//

.SECONDARY : modules/tts-adapter-acapela/.release
modules/tts-adapter-acapela/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("tts-adapter-acapela");

modules/tts-adapter-acapela/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/tts-adapter-acapela/.clean
.PHONY : modules/tts-adapter-acapela/.clean
modules/tts-adapter-acapela/.clean :
	rm("modules/tts-adapter-acapela/target");
