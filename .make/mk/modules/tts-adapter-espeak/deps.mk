modules/tts-adapter-espeak/VERSION := 3.0.19-SNAPSHOT

$(TARGET_DIR)/state/modules/tts-adapter-espeak/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/tts-adapter-espeak/modified-since-release_ : modules/tts-adapter-espeak/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/tts-adapter-espeak/.test
modules/tts-adapter-espeak/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-espeak/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-espeak/3.0.19-SNAPSHOT/tts-adapter-espeak-3.0.19-SNAPSHOT.pom : modules/tts-adapter-espeak/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-espeak/3.0.19-SNAPSHOT/tts-adapter-espeak-3.0.19-SNAPSHOT% : modules/tts-adapter-espeak/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/tts-adapter-espeak/.install.pom
modules/tts-adapter-espeak/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/tts-adapter-espeak");

modules/tts-adapter-espeak/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/tts-adapter-espeak/.install.jar
modules/tts-adapter-espeak/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/tts-adapter-espeak/.install
modules/tts-adapter-espeak/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-espeak/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/tts-adapter-espeak/.install-doc.jar
modules/tts-adapter-espeak/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/tts-adapter-espeak/.install-xprocdoc.jar
modules/tts-adapter-espeak/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/tts-adapter-espeak/.install-doc
modules/tts-adapter-espeak/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-espeak/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/tts-adapter-espeak/.compile-dependencies modules/tts-adapter-espeak/.test-dependencies
modules/tts-adapter-espeak/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/tts-adapter-espeak/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-espeak/3.0.19/tts-adapter-espeak-3.0.19.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-espeak/3.0.19/tts-adapter-espeak-3.0.19-% : modules/tts-adapter-espeak/.release
	+//

.SECONDARY : modules/tts-adapter-espeak/.release
modules/tts-adapter-espeak/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("tts-adapter-espeak");

modules/tts-adapter-espeak/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/tts-adapter-espeak/.clean
.PHONY : modules/tts-adapter-espeak/.clean
modules/tts-adapter-espeak/.clean :
	rm("modules/tts-adapter-espeak/target");
