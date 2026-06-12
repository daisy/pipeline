modules/epubcheck-adapter/VERSION := 1.1.15-SNAPSHOT

$(TARGET_DIR)/state/modules/epubcheck-adapter/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/epubcheck-adapter/modified-since-release_ : modules/epubcheck-adapter/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/epubcheck-adapter/.test
modules/epubcheck-adapter/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/epubcheck-adapter/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epubcheck-adapter/1.1.15-SNAPSHOT/epubcheck-adapter-1.1.15-SNAPSHOT.pom : modules/epubcheck-adapter/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epubcheck-adapter/1.1.15-SNAPSHOT/epubcheck-adapter-1.1.15-SNAPSHOT% : modules/epubcheck-adapter/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/epubcheck-adapter/.install.pom
modules/epubcheck-adapter/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/epubcheck-adapter");

modules/epubcheck-adapter/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/epubcheck-adapter/.install.jar
modules/epubcheck-adapter/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/epubcheck-adapter/.install
modules/epubcheck-adapter/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/epubcheck-adapter/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/epubcheck-adapter/.install-doc.jar
modules/epubcheck-adapter/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/epubcheck-adapter/.install-xprocdoc.jar
modules/epubcheck-adapter/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/epubcheck-adapter/.install-doc
modules/epubcheck-adapter/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/epubcheck-adapter/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/epubcheck-adapter/.compile-dependencies modules/epubcheck-adapter/.test-dependencies
modules/epubcheck-adapter/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.5-SNAPSHOT/modules-parent-1.15.5-SNAPSHOT.pom
modules/epubcheck-adapter/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epubcheck-adapter/1.1.15/epubcheck-adapter-1.1.15.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epubcheck-adapter/1.1.15/epubcheck-adapter-1.1.15-% : modules/epubcheck-adapter/.release
	+//

.SECONDARY : modules/epubcheck-adapter/.release
modules/epubcheck-adapter/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("epubcheck-adapter");

modules/epubcheck-adapter/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.5/modules-parent-1.15.5.pom

clean : modules/epubcheck-adapter/.clean
.PHONY : modules/epubcheck-adapter/.clean
modules/epubcheck-adapter/.clean :
	rm("modules/epubcheck-adapter/target");
