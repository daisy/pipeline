modules/pandoc-adapter/VERSION := 1.0.2-SNAPSHOT

$(TARGET_DIR)/state/modules/pandoc-adapter/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/pandoc-adapter/modified-since-release_ : modules/pandoc-adapter/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/pandoc-adapter/.test
modules/pandoc-adapter/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/pandoc-adapter/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/pandoc-adapter/1.0.2-SNAPSHOT/pandoc-adapter-1.0.2-SNAPSHOT.pom : modules/pandoc-adapter/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/pandoc-adapter/1.0.2-SNAPSHOT/pandoc-adapter-1.0.2-SNAPSHOT% : modules/pandoc-adapter/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/pandoc-adapter/.install.pom
modules/pandoc-adapter/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/pandoc-adapter");

modules/pandoc-adapter/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/pandoc-adapter/.install.jar
modules/pandoc-adapter/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/pandoc-adapter/.install
modules/pandoc-adapter/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/pandoc-adapter/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/pandoc-adapter/.install-doc.jar
modules/pandoc-adapter/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/pandoc-adapter/.install-xprocdoc.jar
modules/pandoc-adapter/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/pandoc-adapter/.install-doc
modules/pandoc-adapter/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/pandoc-adapter/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/pandoc-adapter/.compile-dependencies modules/pandoc-adapter/.test-dependencies
modules/pandoc-adapter/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/pandoc-adapter/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/pandoc-adapter/1.0.2/pandoc-adapter-1.0.2.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/pandoc-adapter/1.0.2/pandoc-adapter-1.0.2-% : modules/pandoc-adapter/.release
	+//

.SECONDARY : modules/pandoc-adapter/.release
modules/pandoc-adapter/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("pandoc-adapter");

modules/pandoc-adapter/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/pandoc-adapter/.clean
.PHONY : modules/pandoc-adapter/.clean
modules/pandoc-adapter/.clean :
	rm("modules/pandoc-adapter/target");
