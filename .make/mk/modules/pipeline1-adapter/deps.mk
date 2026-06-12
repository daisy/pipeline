modules/pipeline1-adapter/VERSION := 1.1.4-SNAPSHOT

$(TARGET_DIR)/state/modules/pipeline1-adapter/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/pipeline1-adapter/modified-since-release_ : modules/pipeline1-adapter/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/pipeline1-adapter/.test
modules/pipeline1-adapter/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/pipeline1-adapter/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/pipeline1-adapter/1.1.4-SNAPSHOT/pipeline1-adapter-1.1.4-SNAPSHOT.pom : modules/pipeline1-adapter/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/pipeline1-adapter/1.1.4-SNAPSHOT/pipeline1-adapter-1.1.4-SNAPSHOT% : modules/pipeline1-adapter/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/pipeline1-adapter/.install.pom
modules/pipeline1-adapter/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/pipeline1-adapter");

modules/pipeline1-adapter/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/pipeline1-adapter/.install.jar
modules/pipeline1-adapter/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/pipeline1-adapter/.install
modules/pipeline1-adapter/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/pipeline1-adapter/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/pipeline1-adapter/.install-doc.jar
modules/pipeline1-adapter/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/pipeline1-adapter/.install-xprocdoc.jar
modules/pipeline1-adapter/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/pipeline1-adapter/.install-doc
modules/pipeline1-adapter/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/pipeline1-adapter/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/pipeline1-adapter/.compile-dependencies modules/pipeline1-adapter/.test-dependencies
modules/pipeline1-adapter/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/pipeline1-adapter/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/pipeline1-adapter/1.1.4/pipeline1-adapter-1.1.4.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/pipeline1-adapter/1.1.4/pipeline1-adapter-1.1.4-% : modules/pipeline1-adapter/.release
	+//

.SECONDARY : modules/pipeline1-adapter/.release
modules/pipeline1-adapter/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("pipeline1-adapter");

modules/pipeline1-adapter/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/pipeline1-adapter/.clean
.PHONY : modules/pipeline1-adapter/.clean
modules/pipeline1-adapter/.clean :
	rm("modules/pipeline1-adapter/target");
