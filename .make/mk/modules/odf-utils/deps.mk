modules/odf-utils/VERSION := 1.0.9-SNAPSHOT

$(TARGET_DIR)/state/modules/odf-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/odf-utils/modified-since-release_ : modules/odf-utils/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/odf-utils/.test
modules/odf-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/odf-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/odf-utils/1.0.9-SNAPSHOT/odf-utils-1.0.9-SNAPSHOT.pom : modules/odf-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/odf-utils/1.0.9-SNAPSHOT/odf-utils-1.0.9-SNAPSHOT% : modules/odf-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/odf-utils/.install.pom
modules/odf-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/odf-utils");

modules/odf-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/odf-utils/.install.jar
modules/odf-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/odf-utils/.install
modules/odf-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/odf-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/odf-utils/.install-doc.jar
modules/odf-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/odf-utils/.install-xprocdoc.jar
modules/odf-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/odf-utils/.install-doc
modules/odf-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/odf-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/odf-utils/.compile-dependencies modules/odf-utils/.test-dependencies
modules/odf-utils/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/odf-utils/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/odf-utils/1.0.9/odf-utils-1.0.9.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/odf-utils/1.0.9/odf-utils-1.0.9-% : modules/odf-utils/.release
	+//

.SECONDARY : modules/odf-utils/.release
modules/odf-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("odf-utils");

modules/odf-utils/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/odf-utils/.clean
.PHONY : modules/odf-utils/.clean
modules/odf-utils/.clean :
	rm("modules/odf-utils/target");
