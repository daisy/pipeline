modules/tts-adapter-sapinative/VERSION := 3.2.4-SNAPSHOT

$(TARGET_DIR)/state/modules/tts-adapter-sapinative/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/tts-adapter-sapinative/modified-since-release_ : modules/tts-adapter-sapinative/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/tts-adapter-sapinative/.test
modules/tts-adapter-sapinative/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-sapinative/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-sapinative/3.2.4-SNAPSHOT/tts-adapter-sapinative-3.2.4-SNAPSHOT.pom : modules/tts-adapter-sapinative/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-sapinative/3.2.4-SNAPSHOT/tts-adapter-sapinative-3.2.4-SNAPSHOT% : modules/tts-adapter-sapinative/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/tts-adapter-sapinative/.install.pom
modules/tts-adapter-sapinative/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/tts-adapter-sapinative");

modules/tts-adapter-sapinative/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/tts-adapter-sapinative/.install.jar
modules/tts-adapter-sapinative/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/tts-adapter-sapinative/.install
modules/tts-adapter-sapinative/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-sapinative/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/tts-adapter-sapinative/.install-doc.jar
modules/tts-adapter-sapinative/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/tts-adapter-sapinative/.install-xprocdoc.jar
modules/tts-adapter-sapinative/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/tts-adapter-sapinative/.install-doc
modules/tts-adapter-sapinative/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-sapinative/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/tts-adapter-sapinative/.compile-dependencies modules/tts-adapter-sapinative/.test-dependencies
modules/tts-adapter-sapinative/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/tts-adapter-sapinative/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-sapinative/3.2.4/tts-adapter-sapinative-3.2.4.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-sapinative/3.2.4/tts-adapter-sapinative-3.2.4-% : modules/tts-adapter-sapinative/.release
	+//

.SECONDARY : modules/tts-adapter-sapinative/.release
modules/tts-adapter-sapinative/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("tts-adapter-sapinative");

modules/tts-adapter-sapinative/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/tts-adapter-sapinative/.clean
.PHONY : modules/tts-adapter-sapinative/.clean
modules/tts-adapter-sapinative/.clean :
	rm("modules/tts-adapter-sapinative/target");
