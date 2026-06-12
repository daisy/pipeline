modules/html-to-dtbook/VERSION := 2.0.12-SNAPSHOT

$(TARGET_DIR)/state/modules/html-to-dtbook/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/html-to-dtbook/modified-since-release_ : modules/html-to-dtbook/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/html-to-dtbook/.test
modules/html-to-dtbook/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/html-to-dtbook/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/html-to-dtbook/2.0.12-SNAPSHOT/html-to-dtbook-2.0.12-SNAPSHOT.pom : modules/html-to-dtbook/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/html-to-dtbook/2.0.12-SNAPSHOT/html-to-dtbook-2.0.12-SNAPSHOT% : modules/html-to-dtbook/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/html-to-dtbook/.install.pom
modules/html-to-dtbook/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/html-to-dtbook");

modules/html-to-dtbook/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/html-to-dtbook/.install.jar
modules/html-to-dtbook/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/html-to-dtbook/.install
modules/html-to-dtbook/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/html-to-dtbook/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/html-to-dtbook/.install-doc.jar
modules/html-to-dtbook/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/html-to-dtbook/.install-xprocdoc.jar
modules/html-to-dtbook/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/html-to-dtbook/.install-doc
modules/html-to-dtbook/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/html-to-dtbook/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/html-to-dtbook/.compile-dependencies modules/html-to-dtbook/.test-dependencies
modules/html-to-dtbook/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/html-to-dtbook/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/html-to-dtbook/2.0.12/html-to-dtbook-2.0.12.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/html-to-dtbook/2.0.12/html-to-dtbook-2.0.12-% : modules/html-to-dtbook/.release
	+//

.SECONDARY : modules/html-to-dtbook/.release
modules/html-to-dtbook/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("html-to-dtbook");

modules/html-to-dtbook/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/html-to-dtbook/.clean
.PHONY : modules/html-to-dtbook/.clean
modules/html-to-dtbook/.clean :
	rm("modules/html-to-dtbook/target");
