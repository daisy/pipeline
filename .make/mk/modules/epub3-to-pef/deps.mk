modules/epub3-to-pef/VERSION := 10.0.2-SNAPSHOT

$(TARGET_DIR)/state/modules/epub3-to-pef/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/epub3-to-pef/modified-since-release_ : modules/epub3-to-pef/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/epub3-to-pef/.test
modules/epub3-to-pef/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/epub3-to-pef/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/epub3-to-pef/10.0.2-SNAPSHOT/epub3-to-pef-10.0.2-SNAPSHOT.pom : modules/epub3-to-pef/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/epub3-to-pef/10.0.2-SNAPSHOT/epub3-to-pef-10.0.2-SNAPSHOT% : modules/epub3-to-pef/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/epub3-to-pef/.install.pom
modules/epub3-to-pef/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/epub3-to-pef");

modules/epub3-to-pef/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/epub3-to-pef/.install.jar
modules/epub3-to-pef/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/epub3-to-pef/.install
modules/epub3-to-pef/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/epub3-to-pef/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/epub3-to-pef/.install-doc.jar
modules/epub3-to-pef/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/epub3-to-pef/.install-xprocdoc.jar
modules/epub3-to-pef/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/epub3-to-pef/.install-doc
modules/epub3-to-pef/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/epub3-to-pef/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/epub3-to-pef/.compile-dependencies modules/epub3-to-pef/.test-dependencies
modules/epub3-to-pef/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/epub3-to-pef/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/epub3-to-pef/10.0.2/epub3-to-pef-10.0.2.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/epub3-to-pef/10.0.2/epub3-to-pef-10.0.2-% : modules/epub3-to-pef/.release
	+//

.SECONDARY : modules/epub3-to-pef/.release
modules/epub3-to-pef/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("epub3-to-pef");

modules/epub3-to-pef/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/epub3-to-pef/.clean
.PHONY : modules/epub3-to-pef/.clean
modules/epub3-to-pef/.clean :
	rm("modules/epub3-to-pef/target");
