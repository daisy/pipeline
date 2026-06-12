modules/tts-adapter-aws/VERSION := 1.0.4-SNAPSHOT

$(TARGET_DIR)/state/modules/tts-adapter-aws/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/tts-adapter-aws/modified-since-release_ : modules/tts-adapter-aws/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/tts-adapter-aws/.test
modules/tts-adapter-aws/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-aws/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-aws/1.0.4-SNAPSHOT/tts-adapter-aws-1.0.4-SNAPSHOT.pom : modules/tts-adapter-aws/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-aws/1.0.4-SNAPSHOT/tts-adapter-aws-1.0.4-SNAPSHOT% : modules/tts-adapter-aws/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/tts-adapter-aws/.install.pom
modules/tts-adapter-aws/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/tts-adapter-aws");

modules/tts-adapter-aws/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/tts-adapter-aws/.install.jar
modules/tts-adapter-aws/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/tts-adapter-aws/.install
modules/tts-adapter-aws/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-aws/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/tts-adapter-aws/.install-doc.jar
modules/tts-adapter-aws/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/tts-adapter-aws/.install-xprocdoc.jar
modules/tts-adapter-aws/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/tts-adapter-aws/.install-doc
modules/tts-adapter-aws/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-aws/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/tts-adapter-aws/.compile-dependencies modules/tts-adapter-aws/.test-dependencies
modules/tts-adapter-aws/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/tts-adapter-aws/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-aws/1.0.4/tts-adapter-aws-1.0.4.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-aws/1.0.4/tts-adapter-aws-1.0.4-% : modules/tts-adapter-aws/.release
	+//

.SECONDARY : modules/tts-adapter-aws/.release
modules/tts-adapter-aws/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("tts-adapter-aws");

modules/tts-adapter-aws/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/tts-adapter-aws/.clean
.PHONY : modules/tts-adapter-aws/.clean
modules/tts-adapter-aws/.clean :
	rm("modules/tts-adapter-aws/target");
