modules/metadata-utils/VERSION := 2.0.3-SNAPSHOT

$(TARGET_DIR)/state/modules/metadata-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/metadata-utils/modified-since-release_ : modules/metadata-utils/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/metadata-utils/.test
modules/metadata-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/metadata-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/metadata-utils/2.0.3-SNAPSHOT/metadata-utils-2.0.3-SNAPSHOT.pom : modules/metadata-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/metadata-utils/2.0.3-SNAPSHOT/metadata-utils-2.0.3-SNAPSHOT% : modules/metadata-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/metadata-utils/.install.pom
modules/metadata-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/metadata-utils");

modules/metadata-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/metadata-utils/.install.jar
modules/metadata-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/metadata-utils/.install
modules/metadata-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/metadata-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/metadata-utils/.install-doc.jar
modules/metadata-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/metadata-utils/.install-xprocdoc.jar
modules/metadata-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/metadata-utils/.install-doc
modules/metadata-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/metadata-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/metadata-utils/.compile-dependencies modules/metadata-utils/.test-dependencies
modules/metadata-utils/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.5-SNAPSHOT/modules-parent-1.15.5-SNAPSHOT.pom
modules/metadata-utils/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/metadata-utils/2.0.3/metadata-utils-2.0.3.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/metadata-utils/2.0.3/metadata-utils-2.0.3-% : modules/metadata-utils/.release
	+//

.SECONDARY : modules/metadata-utils/.release
modules/metadata-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("metadata-utils");

modules/metadata-utils/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.5/modules-parent-1.15.5.pom

clean : modules/metadata-utils/.clean
.PHONY : modules/metadata-utils/.clean
modules/metadata-utils/.clean :
	rm("modules/metadata-utils/target");
