modules/dtbook-to-zedai/VERSION := 4.2.2-SNAPSHOT

$(TARGET_DIR)/state/modules/dtbook-to-zedai/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/dtbook-to-zedai/modified-since-release_ : modules/dtbook-to-zedai/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/dtbook-to-zedai/.test
modules/dtbook-to-zedai/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/dtbook-to-zedai/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-zedai/4.2.2-SNAPSHOT/dtbook-to-zedai-4.2.2-SNAPSHOT.pom : modules/dtbook-to-zedai/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-zedai/4.2.2-SNAPSHOT/dtbook-to-zedai-4.2.2-SNAPSHOT% : modules/dtbook-to-zedai/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/dtbook-to-zedai/.install.pom
modules/dtbook-to-zedai/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/dtbook-to-zedai");

modules/dtbook-to-zedai/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/dtbook-to-zedai/.install.jar
modules/dtbook-to-zedai/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/dtbook-to-zedai/.install
modules/dtbook-to-zedai/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/dtbook-to-zedai/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/dtbook-to-zedai/.install-doc.jar
modules/dtbook-to-zedai/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/dtbook-to-zedai/.install-xprocdoc.jar
modules/dtbook-to-zedai/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/dtbook-to-zedai/.install-doc
modules/dtbook-to-zedai/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/dtbook-to-zedai/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/dtbook-to-zedai/.compile-dependencies modules/dtbook-to-zedai/.test-dependencies
modules/dtbook-to-zedai/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/dtbook-to-zedai/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-zedai/4.2.2/dtbook-to-zedai-4.2.2.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-zedai/4.2.2/dtbook-to-zedai-4.2.2-% : modules/dtbook-to-zedai/.release
	+//

.SECONDARY : modules/dtbook-to-zedai/.release
modules/dtbook-to-zedai/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("dtbook-to-zedai");

modules/dtbook-to-zedai/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/dtbook-to-zedai/.clean
.PHONY : modules/dtbook-to-zedai/.clean
modules/dtbook-to-zedai/.clean :
	rm("modules/dtbook-to-zedai/target");
