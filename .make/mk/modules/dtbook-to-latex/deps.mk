modules/dtbook-to-latex/VERSION := 1.0.1-SNAPSHOT

$(TARGET_DIR)/state/modules/dtbook-to-latex/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/dtbook-to-latex/modified-since-release_ : modules/dtbook-to-latex/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/dtbook-to-latex/.test
modules/dtbook-to-latex/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/dtbook-to-latex/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-latex/1.0.1-SNAPSHOT/dtbook-to-latex-1.0.1-SNAPSHOT.pom : modules/dtbook-to-latex/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-latex/1.0.1-SNAPSHOT/dtbook-to-latex-1.0.1-SNAPSHOT% : modules/dtbook-to-latex/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/dtbook-to-latex/.install.pom
modules/dtbook-to-latex/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/dtbook-to-latex");

modules/dtbook-to-latex/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/dtbook-to-latex/.install.jar
modules/dtbook-to-latex/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/dtbook-to-latex/.install
modules/dtbook-to-latex/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/dtbook-to-latex/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/dtbook-to-latex/.install-doc.jar
modules/dtbook-to-latex/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/dtbook-to-latex/.install-xprocdoc.jar
modules/dtbook-to-latex/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/dtbook-to-latex/.install-doc
modules/dtbook-to-latex/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/dtbook-to-latex/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/dtbook-to-latex/.compile-dependencies modules/dtbook-to-latex/.test-dependencies
modules/dtbook-to-latex/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/dtbook-to-latex/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-latex/1.0.1/dtbook-to-latex-1.0.1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-latex/1.0.1/dtbook-to-latex-1.0.1-% : modules/dtbook-to-latex/.release
	+//

.SECONDARY : modules/dtbook-to-latex/.release
modules/dtbook-to-latex/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("dtbook-to-latex");

modules/dtbook-to-latex/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/dtbook-to-latex/.clean
.PHONY : modules/dtbook-to-latex/.clean
modules/dtbook-to-latex/.clean :
	rm("modules/dtbook-to-latex/target");
