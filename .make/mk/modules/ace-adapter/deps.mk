modules/ace-adapter/VERSION := 1.0.14-SNAPSHOT

$(TARGET_DIR)/state/modules/ace-adapter/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/ace-adapter/modified-since-release_ : modules/ace-adapter/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/ace-adapter/.test
modules/ace-adapter/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/ace-adapter/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/ace-adapter/1.0.14-SNAPSHOT/ace-adapter-1.0.14-SNAPSHOT.pom : modules/ace-adapter/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/ace-adapter/1.0.14-SNAPSHOT/ace-adapter-1.0.14-SNAPSHOT% : modules/ace-adapter/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/ace-adapter/.install.pom
modules/ace-adapter/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/ace-adapter");

modules/ace-adapter/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/ace-adapter/.install.jar
modules/ace-adapter/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/ace-adapter/.install
modules/ace-adapter/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/ace-adapter/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/ace-adapter/.install-doc.jar
modules/ace-adapter/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/ace-adapter/.install-xprocdoc.jar
modules/ace-adapter/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/ace-adapter/.install-doc
modules/ace-adapter/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/ace-adapter/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/ace-adapter/.compile-dependencies modules/ace-adapter/.test-dependencies
modules/ace-adapter/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/ace-adapter/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/ace-adapter/1.0.14/ace-adapter-1.0.14.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/ace-adapter/1.0.14/ace-adapter-1.0.14-% : modules/ace-adapter/.release
	+//

.SECONDARY : modules/ace-adapter/.release
modules/ace-adapter/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("ace-adapter");

modules/ace-adapter/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/ace-adapter/.clean
.PHONY : modules/ace-adapter/.clean
modules/ace-adapter/.clean :
	rm("modules/ace-adapter/target");
