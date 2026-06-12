modules/dtbook-to-ebraille/VERSION := 1.3.1-SNAPSHOT

$(TARGET_DIR)/state/modules/dtbook-to-ebraille/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/dtbook-to-ebraille/modified-since-release_ : modules/dtbook-to-ebraille/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/dtbook-to-ebraille/.test
modules/dtbook-to-ebraille/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/dtbook-to-ebraille/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-ebraille/1.3.1-SNAPSHOT/dtbook-to-ebraille-1.3.1-SNAPSHOT.pom : modules/dtbook-to-ebraille/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-ebraille/1.3.1-SNAPSHOT/dtbook-to-ebraille-1.3.1-SNAPSHOT% : modules/dtbook-to-ebraille/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/dtbook-to-ebraille/.install.pom
modules/dtbook-to-ebraille/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/dtbook-to-ebraille");

modules/dtbook-to-ebraille/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/dtbook-to-ebraille/.install.jar
modules/dtbook-to-ebraille/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/dtbook-to-ebraille/.install
modules/dtbook-to-ebraille/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/dtbook-to-ebraille/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/dtbook-to-ebraille/.install-doc.jar
modules/dtbook-to-ebraille/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/dtbook-to-ebraille/.install-xprocdoc.jar
modules/dtbook-to-ebraille/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/dtbook-to-ebraille/.install-doc
modules/dtbook-to-ebraille/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/dtbook-to-ebraille/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/dtbook-to-ebraille/.compile-dependencies modules/dtbook-to-ebraille/.test-dependencies
modules/dtbook-to-ebraille/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/dtbook-to-ebraille/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-ebraille/1.3.1/dtbook-to-ebraille-1.3.1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-ebraille/1.3.1/dtbook-to-ebraille-1.3.1-% : modules/dtbook-to-ebraille/.release
	+//

.SECONDARY : modules/dtbook-to-ebraille/.release
modules/dtbook-to-ebraille/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("dtbook-to-ebraille");

modules/dtbook-to-ebraille/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/dtbook-to-ebraille/.clean
.PHONY : modules/dtbook-to-ebraille/.clean
modules/dtbook-to-ebraille/.clean :
	rm("modules/dtbook-to-ebraille/target");
