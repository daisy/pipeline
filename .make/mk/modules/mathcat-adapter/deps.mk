modules/mathcat-adapter/VERSION := 1.0.3-SNAPSHOT

$(TARGET_DIR)/state/modules/mathcat-adapter/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/mathcat-adapter/modified-since-release_ : modules/mathcat-adapter/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/mathcat-adapter/.test
modules/mathcat-adapter/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/mathcat-adapter/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mathcat-adapter/1.0.3-SNAPSHOT/mathcat-adapter-1.0.3-SNAPSHOT.pom : modules/mathcat-adapter/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mathcat-adapter/1.0.3-SNAPSHOT/mathcat-adapter-1.0.3-SNAPSHOT% : modules/mathcat-adapter/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/mathcat-adapter/.install.pom
modules/mathcat-adapter/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/mathcat-adapter");

modules/mathcat-adapter/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/mathcat-adapter/.install.jar
modules/mathcat-adapter/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/mathcat-adapter/.install
modules/mathcat-adapter/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/mathcat-adapter/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/mathcat-adapter/.install-doc.jar
modules/mathcat-adapter/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/mathcat-adapter/.install-xprocdoc.jar
modules/mathcat-adapter/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/mathcat-adapter/.install-doc
modules/mathcat-adapter/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/mathcat-adapter/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/mathcat-adapter/.compile-dependencies modules/mathcat-adapter/.test-dependencies
modules/mathcat-adapter/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/mathcat-adapter/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mathcat-adapter/1.0.3/mathcat-adapter-1.0.3.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mathcat-adapter/1.0.3/mathcat-adapter-1.0.3-% : modules/mathcat-adapter/.release
	+//

.SECONDARY : modules/mathcat-adapter/.release
modules/mathcat-adapter/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("mathcat-adapter");

modules/mathcat-adapter/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/mathcat-adapter/.clean
.PHONY : modules/mathcat-adapter/.clean
modules/mathcat-adapter/.clean :
	rm("modules/mathcat-adapter/target");
