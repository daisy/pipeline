modules/html-utils/VERSION := 6.6.2-SNAPSHOT

$(TARGET_DIR)/state/modules/html-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/html-utils/modified-since-release_ : modules/html-utils/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/html-utils/.test
modules/html-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/html-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/html-utils/6.6.2-SNAPSHOT/html-utils-6.6.2-SNAPSHOT.pom : modules/html-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/html-utils/6.6.2-SNAPSHOT/html-utils-6.6.2-SNAPSHOT% : modules/html-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/html-utils/.install.pom
modules/html-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/html-utils");

modules/html-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/html-utils/.install.jar
modules/html-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/html-utils/.install
modules/html-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/html-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/html-utils/.install-doc.jar
modules/html-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/html-utils/.install-xprocdoc.jar
modules/html-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/html-utils/.install-doc
modules/html-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/html-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/html-utils/.compile-dependencies modules/html-utils/.test-dependencies
modules/html-utils/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/html-utils/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/html-utils/6.6.2/html-utils-6.6.2.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/html-utils/6.6.2/html-utils-6.6.2-% : modules/html-utils/.release
	+//

.SECONDARY : modules/html-utils/.release
modules/html-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("html-utils");

modules/html-utils/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/html-utils/.clean
.PHONY : modules/html-utils/.clean
modules/html-utils/.clean :
	rm("modules/html-utils/target");
