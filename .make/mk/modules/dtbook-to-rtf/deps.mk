modules/dtbook-to-rtf/VERSION := 2.0.16-SNAPSHOT

$(TARGET_DIR)/state/modules/dtbook-to-rtf/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/dtbook-to-rtf/modified-since-release_ : modules/dtbook-to-rtf/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/dtbook-to-rtf/.test
modules/dtbook-to-rtf/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/dtbook-to-rtf/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-rtf/2.0.16-SNAPSHOT/dtbook-to-rtf-2.0.16-SNAPSHOT.pom : modules/dtbook-to-rtf/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-rtf/2.0.16-SNAPSHOT/dtbook-to-rtf-2.0.16-SNAPSHOT% : modules/dtbook-to-rtf/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/dtbook-to-rtf/.install.pom
modules/dtbook-to-rtf/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/dtbook-to-rtf");

modules/dtbook-to-rtf/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/dtbook-to-rtf/.install.jar
modules/dtbook-to-rtf/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/dtbook-to-rtf/.install
modules/dtbook-to-rtf/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/dtbook-to-rtf/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/dtbook-to-rtf/.install-doc.jar
modules/dtbook-to-rtf/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/dtbook-to-rtf/.install-xprocdoc.jar
modules/dtbook-to-rtf/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/dtbook-to-rtf/.install-doc
modules/dtbook-to-rtf/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/dtbook-to-rtf/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/dtbook-to-rtf/.compile-dependencies modules/dtbook-to-rtf/.test-dependencies
modules/dtbook-to-rtf/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/dtbook-to-rtf/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-rtf/2.0.16/dtbook-to-rtf-2.0.16.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-rtf/2.0.16/dtbook-to-rtf-2.0.16-% : modules/dtbook-to-rtf/.release
	+//

.SECONDARY : modules/dtbook-to-rtf/.release
modules/dtbook-to-rtf/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("dtbook-to-rtf");

modules/dtbook-to-rtf/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/dtbook-to-rtf/.clean
.PHONY : modules/dtbook-to-rtf/.clean
modules/dtbook-to-rtf/.clean :
	rm("modules/dtbook-to-rtf/target");
