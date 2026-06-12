modules/zedai-to-pef/VERSION := 7.1.2-SNAPSHOT

$(TARGET_DIR)/state/modules/zedai-to-pef/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/zedai-to-pef/modified-since-release_ : modules/zedai-to-pef/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/zedai-to-pef/.test
modules/zedai-to-pef/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/zedai-to-pef/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/zedai-to-pef/7.1.2-SNAPSHOT/zedai-to-pef-7.1.2-SNAPSHOT.pom : modules/zedai-to-pef/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/zedai-to-pef/7.1.2-SNAPSHOT/zedai-to-pef-7.1.2-SNAPSHOT% : modules/zedai-to-pef/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/zedai-to-pef/.install.pom
modules/zedai-to-pef/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/zedai-to-pef");

modules/zedai-to-pef/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/zedai-to-pef/.install.jar
modules/zedai-to-pef/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/zedai-to-pef/.install
modules/zedai-to-pef/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/zedai-to-pef/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/zedai-to-pef/.install-doc.jar
modules/zedai-to-pef/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/zedai-to-pef/.install-xprocdoc.jar
modules/zedai-to-pef/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/zedai-to-pef/.install-doc
modules/zedai-to-pef/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/zedai-to-pef/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/zedai-to-pef/.compile-dependencies modules/zedai-to-pef/.test-dependencies
modules/zedai-to-pef/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/zedai-to-pef/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/zedai-to-pef/7.1.2/zedai-to-pef-7.1.2.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/zedai-to-pef/7.1.2/zedai-to-pef-7.1.2-% : modules/zedai-to-pef/.release
	+//

.SECONDARY : modules/zedai-to-pef/.release
modules/zedai-to-pef/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("zedai-to-pef");

modules/zedai-to-pef/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/zedai-to-pef/.clean
.PHONY : modules/zedai-to-pef/.clean
modules/zedai-to-pef/.clean :
	rm("modules/zedai-to-pef/target");
