modules/html-to-pef/VERSION := 10.0.2-SNAPSHOT

$(TARGET_DIR)/state/modules/html-to-pef/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/html-to-pef/modified-since-release_ : modules/html-to-pef/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/html-to-pef/.test
modules/html-to-pef/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/html-to-pef/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/html-to-pef/10.0.2-SNAPSHOT/html-to-pef-10.0.2-SNAPSHOT.pom : modules/html-to-pef/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/html-to-pef/10.0.2-SNAPSHOT/html-to-pef-10.0.2-SNAPSHOT% : modules/html-to-pef/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/html-to-pef/.install.pom
modules/html-to-pef/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/html-to-pef");

modules/html-to-pef/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/html-to-pef/.install.jar
modules/html-to-pef/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/html-to-pef/.install
modules/html-to-pef/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/html-to-pef/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/html-to-pef/.install-doc.jar
modules/html-to-pef/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/html-to-pef/.install-xprocdoc.jar
modules/html-to-pef/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/html-to-pef/.install-doc
modules/html-to-pef/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/html-to-pef/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/html-to-pef/.compile-dependencies modules/html-to-pef/.test-dependencies
modules/html-to-pef/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/html-to-pef/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/html-to-pef/10.0.2/html-to-pef-10.0.2.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/html-to-pef/10.0.2/html-to-pef-10.0.2-% : modules/html-to-pef/.release
	+//

.SECONDARY : modules/html-to-pef/.release
modules/html-to-pef/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("html-to-pef");

modules/html-to-pef/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/html-to-pef/.clean
.PHONY : modules/html-to-pef/.clean
modules/html-to-pef/.clean :
	rm("modules/html-to-pef/target");
