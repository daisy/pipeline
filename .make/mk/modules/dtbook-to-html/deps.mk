modules/dtbook-to-html/VERSION := 4.1.2-SNAPSHOT

$(TARGET_DIR)/state/modules/dtbook-to-html/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/dtbook-to-html/modified-since-release_ : modules/dtbook-to-html/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/dtbook-to-html/.test
modules/dtbook-to-html/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/dtbook-to-html/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-html/4.1.2-SNAPSHOT/dtbook-to-html-4.1.2-SNAPSHOT.pom : modules/dtbook-to-html/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-html/4.1.2-SNAPSHOT/dtbook-to-html-4.1.2-SNAPSHOT% : modules/dtbook-to-html/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/dtbook-to-html/.install.pom
modules/dtbook-to-html/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/dtbook-to-html");

modules/dtbook-to-html/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/dtbook-to-html/.install.jar
modules/dtbook-to-html/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/dtbook-to-html/.install
modules/dtbook-to-html/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/dtbook-to-html/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/dtbook-to-html/.install-doc.jar
modules/dtbook-to-html/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/dtbook-to-html/.install-xprocdoc.jar
modules/dtbook-to-html/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/dtbook-to-html/.install-doc
modules/dtbook-to-html/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/dtbook-to-html/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/dtbook-to-html/.compile-dependencies modules/dtbook-to-html/.test-dependencies
modules/dtbook-to-html/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/dtbook-to-html/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-html/4.1.2/dtbook-to-html-4.1.2.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-html/4.1.2/dtbook-to-html-4.1.2-% : modules/dtbook-to-html/.release
	+//

.SECONDARY : modules/dtbook-to-html/.release
modules/dtbook-to-html/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("dtbook-to-html");

modules/dtbook-to-html/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/dtbook-to-html/.clean
.PHONY : modules/dtbook-to-html/.clean
modules/dtbook-to-html/.clean :
	rm("modules/dtbook-to-html/target");
