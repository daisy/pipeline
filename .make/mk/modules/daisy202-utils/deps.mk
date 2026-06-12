modules/daisy202-utils/VERSION := 1.7.1-SNAPSHOT

$(TARGET_DIR)/state/modules/daisy202-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/daisy202-utils/modified-since-release_ : modules/daisy202-utils/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/daisy202-utils/.test
modules/daisy202-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/daisy202-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy202-utils/1.7.1-SNAPSHOT/daisy202-utils-1.7.1-SNAPSHOT.pom : modules/daisy202-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy202-utils/1.7.1-SNAPSHOT/daisy202-utils-1.7.1-SNAPSHOT% : modules/daisy202-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/daisy202-utils/.install.pom
modules/daisy202-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/daisy202-utils");

modules/daisy202-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/daisy202-utils/.install.jar
modules/daisy202-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/daisy202-utils/.install
modules/daisy202-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/daisy202-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/daisy202-utils/.install-doc.jar
modules/daisy202-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/daisy202-utils/.install-xprocdoc.jar
modules/daisy202-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/daisy202-utils/.install-doc
modules/daisy202-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/daisy202-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/daisy202-utils/.compile-dependencies modules/daisy202-utils/.test-dependencies
modules/daisy202-utils/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/daisy202-utils/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy202-utils/1.7.1/daisy202-utils-1.7.1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy202-utils/1.7.1/daisy202-utils-1.7.1-% : modules/daisy202-utils/.release
	+//

.SECONDARY : modules/daisy202-utils/.release
modules/daisy202-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("daisy202-utils");

modules/daisy202-utils/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/daisy202-utils/.clean
.PHONY : modules/daisy202-utils/.clean
modules/daisy202-utils/.clean :
	rm("modules/daisy202-utils/target");
