modules/dtbook-to-odt/VERSION := 2.2.1-SNAPSHOT

$(TARGET_DIR)/state/modules/dtbook-to-odt/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/dtbook-to-odt/modified-since-release_ : modules/dtbook-to-odt/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/dtbook-to-odt/.test
modules/dtbook-to-odt/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/dtbook-to-odt/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-odt/2.2.1-SNAPSHOT/dtbook-to-odt-2.2.1-SNAPSHOT.pom : modules/dtbook-to-odt/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-odt/2.2.1-SNAPSHOT/dtbook-to-odt-2.2.1-SNAPSHOT% : modules/dtbook-to-odt/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/dtbook-to-odt/.install.pom
modules/dtbook-to-odt/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/dtbook-to-odt");

modules/dtbook-to-odt/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/dtbook-to-odt/.install.jar
modules/dtbook-to-odt/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/dtbook-to-odt/.install
modules/dtbook-to-odt/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/dtbook-to-odt/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/dtbook-to-odt/.install-doc.jar
modules/dtbook-to-odt/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/dtbook-to-odt/.install-xprocdoc.jar
modules/dtbook-to-odt/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/dtbook-to-odt/.install-doc
modules/dtbook-to-odt/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/dtbook-to-odt/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/dtbook-to-odt/.compile-dependencies modules/dtbook-to-odt/.test-dependencies
modules/dtbook-to-odt/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/dtbook-to-odt/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-odt/2.2.1/dtbook-to-odt-2.2.1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-odt/2.2.1/dtbook-to-odt-2.2.1-% : modules/dtbook-to-odt/.release
	+//

.SECONDARY : modules/dtbook-to-odt/.release
modules/dtbook-to-odt/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("dtbook-to-odt");

modules/dtbook-to-odt/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/dtbook-to-odt/.clean
.PHONY : modules/dtbook-to-odt/.clean
modules/dtbook-to-odt/.clean :
	rm("modules/dtbook-to-odt/target");
