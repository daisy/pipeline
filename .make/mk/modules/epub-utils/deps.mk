modules/epub-utils/VERSION := 2.5.1-SNAPSHOT

$(TARGET_DIR)/state/modules/epub-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/epub-utils/modified-since-release_ : modules/epub-utils/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/epub-utils/.test
modules/epub-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/epub-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub-utils/2.5.1-SNAPSHOT/epub-utils-2.5.1-SNAPSHOT.pom : modules/epub-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub-utils/2.5.1-SNAPSHOT/epub-utils-2.5.1-SNAPSHOT% : modules/epub-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/epub-utils/.install.pom
modules/epub-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/epub-utils");

modules/epub-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/epub-utils/.install.jar
modules/epub-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/epub-utils/.install
modules/epub-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/epub-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/epub-utils/.install-doc.jar
modules/epub-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/epub-utils/.install-xprocdoc.jar
modules/epub-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/epub-utils/.install-doc
modules/epub-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/epub-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/epub-utils/.compile-dependencies modules/epub-utils/.test-dependencies
modules/epub-utils/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/epub-utils/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub-utils/2.5.1/epub-utils-2.5.1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub-utils/2.5.1/epub-utils-2.5.1-% : modules/epub-utils/.release
	+//

.SECONDARY : modules/epub-utils/.release
modules/epub-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("epub-utils");

modules/epub-utils/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/epub-utils/.clean
.PHONY : modules/epub-utils/.clean
modules/epub-utils/.clean :
	rm("modules/epub-utils/target");
