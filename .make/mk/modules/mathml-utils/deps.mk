modules/mathml-utils/VERSION := 1.1.3-SNAPSHOT

$(TARGET_DIR)/state/modules/mathml-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/mathml-utils/modified-since-release_ : modules/mathml-utils/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/mathml-utils/.test
modules/mathml-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/mathml-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mathml-utils/1.1.3-SNAPSHOT/mathml-utils-1.1.3-SNAPSHOT.pom : modules/mathml-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mathml-utils/1.1.3-SNAPSHOT/mathml-utils-1.1.3-SNAPSHOT% : modules/mathml-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/mathml-utils/.install.pom
modules/mathml-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/mathml-utils");

modules/mathml-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/mathml-utils/.install.jar
modules/mathml-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/mathml-utils/.install
modules/mathml-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/mathml-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/mathml-utils/.install-doc.jar
modules/mathml-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/mathml-utils/.install-xprocdoc.jar
modules/mathml-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/mathml-utils/.install-doc
modules/mathml-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/mathml-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/mathml-utils/.compile-dependencies modules/mathml-utils/.test-dependencies
modules/mathml-utils/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/mathml-utils/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mathml-utils/1.1.3/mathml-utils-1.1.3.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mathml-utils/1.1.3/mathml-utils-1.1.3-% : modules/mathml-utils/.release
	+//

.SECONDARY : modules/mathml-utils/.release
modules/mathml-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("mathml-utils");

modules/mathml-utils/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/mathml-utils/.clean
.PHONY : modules/mathml-utils/.clean
modules/mathml-utils/.clean :
	rm("modules/mathml-utils/target");
