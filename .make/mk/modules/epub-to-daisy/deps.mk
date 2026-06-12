modules/epub-to-daisy/VERSION := 1.5.2-SNAPSHOT

$(TARGET_DIR)/state/modules/epub-to-daisy/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/epub-to-daisy/modified-since-release_ : modules/epub-to-daisy/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/epub-to-daisy/.test
modules/epub-to-daisy/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/epub-to-daisy/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub-to-daisy/1.5.2-SNAPSHOT/epub-to-daisy-1.5.2-SNAPSHOT.pom : modules/epub-to-daisy/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub-to-daisy/1.5.2-SNAPSHOT/epub-to-daisy-1.5.2-SNAPSHOT% : modules/epub-to-daisy/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/epub-to-daisy/.install.pom
modules/epub-to-daisy/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/epub-to-daisy");

modules/epub-to-daisy/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/epub-to-daisy/.install.jar
modules/epub-to-daisy/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/epub-to-daisy/.install
modules/epub-to-daisy/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/epub-to-daisy/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/epub-to-daisy/.install-doc.jar
modules/epub-to-daisy/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/epub-to-daisy/.install-xprocdoc.jar
modules/epub-to-daisy/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/epub-to-daisy/.install-doc
modules/epub-to-daisy/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/epub-to-daisy/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/epub-to-daisy/.compile-dependencies modules/epub-to-daisy/.test-dependencies
modules/epub-to-daisy/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/epub-to-daisy/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub-to-daisy/1.5.2/epub-to-daisy-1.5.2.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub-to-daisy/1.5.2/epub-to-daisy-1.5.2-% : modules/epub-to-daisy/.release
	+//

.SECONDARY : modules/epub-to-daisy/.release
modules/epub-to-daisy/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("epub-to-daisy");

modules/epub-to-daisy/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/epub-to-daisy/.clean
.PHONY : modules/epub-to-daisy/.clean
modules/epub-to-daisy/.clean :
	rm("modules/epub-to-daisy/target");
