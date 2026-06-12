modules/common-utils/VERSION := 3.4.2-SNAPSHOT

$(TARGET_DIR)/state/modules/common-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/common-utils/modified-since-release_ : modules/common-utils/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/common-utils/.test
modules/common-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/common-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/common-utils/3.4.2-SNAPSHOT/common-utils-3.4.2-SNAPSHOT.pom : modules/common-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/common-utils/3.4.2-SNAPSHOT/common-utils-3.4.2-SNAPSHOT% : modules/common-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/common-utils/.install.pom
modules/common-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/common-utils");

modules/common-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/common-utils/.install.jar
modules/common-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/common-utils/.install
modules/common-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/common-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/common-utils/.install-doc.jar
modules/common-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/common-utils/.install-xprocdoc.jar
modules/common-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/common-utils/.install-doc
modules/common-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/common-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/common-utils/.compile-dependencies modules/common-utils/.test-dependencies
modules/common-utils/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/common-utils/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/common-utils/3.4.2/common-utils-3.4.2.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/common-utils/3.4.2/common-utils-3.4.2-% : modules/common-utils/.release
	+//

.SECONDARY : modules/common-utils/.release
modules/common-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("common-utils");

modules/common-utils/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/common-utils/.clean
.PHONY : modules/common-utils/.clean
modules/common-utils/.clean :
	rm("modules/common-utils/target");
