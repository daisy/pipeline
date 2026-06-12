modules/pef-utils/VERSION := 8.0.3-SNAPSHOT

$(TARGET_DIR)/state/modules/pef-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/pef-utils/modified-since-release_ : modules/pef-utils/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/pef-utils/.test
modules/pef-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/pef-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/pef-utils/8.0.3-SNAPSHOT/pef-utils-8.0.3-SNAPSHOT.pom : modules/pef-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/pef-utils/8.0.3-SNAPSHOT/pef-utils-8.0.3-SNAPSHOT% : modules/pef-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/pef-utils/.install.pom
modules/pef-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/pef-utils");

modules/pef-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/pef-utils/.install.jar
modules/pef-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/pef-utils/.install
modules/pef-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/pef-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/pef-utils/.install-doc.jar
modules/pef-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/pef-utils/.install-xprocdoc.jar
modules/pef-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/pef-utils/.install-javadoc.jar
modules/pef-utils/.install-javadoc.jar : %/.install-javadoc.jar : %/.install-doc

.SECONDARY : modules/pef-utils/.install-doc
modules/pef-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/pef-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/pef-utils/.compile-dependencies modules/pef-utils/.test-dependencies
modules/pef-utils/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/pef-utils/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/pef-utils/8.0.3/pef-utils-8.0.3.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/pef-utils/8.0.3/pef-utils-8.0.3-% : modules/pef-utils/.release
	+//

.SECONDARY : modules/pef-utils/.release
modules/pef-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("pef-utils");

modules/pef-utils/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/pef-utils/.clean
.PHONY : modules/pef-utils/.clean
modules/pef-utils/.clean :
	rm("modules/pef-utils/target");
