modules/dtbook-utils/VERSION := 6.2.1-SNAPSHOT

$(TARGET_DIR)/state/modules/dtbook-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/dtbook-utils/modified-since-release_ : modules/dtbook-utils/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/dtbook-utils/.test
modules/dtbook-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/dtbook-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-utils/6.2.1-SNAPSHOT/dtbook-utils-6.2.1-SNAPSHOT.pom : modules/dtbook-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-utils/6.2.1-SNAPSHOT/dtbook-utils-6.2.1-SNAPSHOT% : modules/dtbook-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/dtbook-utils/.install.pom
modules/dtbook-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/dtbook-utils");

modules/dtbook-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/dtbook-utils/.install.jar
modules/dtbook-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/dtbook-utils/.install
modules/dtbook-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/dtbook-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/dtbook-utils/.install-doc.jar
modules/dtbook-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/dtbook-utils/.install-xprocdoc.jar
modules/dtbook-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/dtbook-utils/.install-doc
modules/dtbook-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/dtbook-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/dtbook-utils/.compile-dependencies modules/dtbook-utils/.test-dependencies
modules/dtbook-utils/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/dtbook-utils/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-utils/6.2.1/dtbook-utils-6.2.1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-utils/6.2.1/dtbook-utils-6.2.1-% : modules/dtbook-utils/.release
	+//

.SECONDARY : modules/dtbook-utils/.release
modules/dtbook-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("dtbook-utils");

modules/dtbook-utils/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/dtbook-utils/.clean
.PHONY : modules/dtbook-utils/.clean
modules/dtbook-utils/.clean :
	rm("modules/dtbook-utils/target");
