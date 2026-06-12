modules/obfl-utils/VERSION := 2.0.3-SNAPSHOT

$(TARGET_DIR)/state/modules/obfl-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/obfl-utils/modified-since-release_ : modules/obfl-utils/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/obfl-utils/.test
modules/obfl-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/obfl-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/obfl-utils/2.0.3-SNAPSHOT/obfl-utils-2.0.3-SNAPSHOT.pom : modules/obfl-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/obfl-utils/2.0.3-SNAPSHOT/obfl-utils-2.0.3-SNAPSHOT% : modules/obfl-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/obfl-utils/.install.pom
modules/obfl-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/obfl-utils");

modules/obfl-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/obfl-utils/.install.jar
modules/obfl-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/obfl-utils/.install
modules/obfl-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/obfl-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/obfl-utils/.install-doc.jar
modules/obfl-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/obfl-utils/.install-xprocdoc.jar
modules/obfl-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/obfl-utils/.install-doc
modules/obfl-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/obfl-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/obfl-utils/.compile-dependencies modules/obfl-utils/.test-dependencies
modules/obfl-utils/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/obfl-utils/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/obfl-utils/2.0.3/obfl-utils-2.0.3.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/obfl-utils/2.0.3/obfl-utils-2.0.3-% : modules/obfl-utils/.release
	+//

.SECONDARY : modules/obfl-utils/.release
modules/obfl-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("obfl-utils");

modules/obfl-utils/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/obfl-utils/.clean
.PHONY : modules/obfl-utils/.clean
modules/obfl-utils/.clean :
	rm("modules/obfl-utils/target");
