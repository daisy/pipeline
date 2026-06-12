modules/ocr-utils/VERSION := 1.1.1-SNAPSHOT

$(TARGET_DIR)/state/modules/ocr-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/ocr-utils/modified-since-release_ : modules/ocr-utils/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/ocr-utils/.test
modules/ocr-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/ocr-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/ocr-utils/1.1.1-SNAPSHOT/ocr-utils-1.1.1-SNAPSHOT.pom : modules/ocr-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/ocr-utils/1.1.1-SNAPSHOT/ocr-utils-1.1.1-SNAPSHOT% : modules/ocr-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/ocr-utils/.install.pom
modules/ocr-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/ocr-utils");

modules/ocr-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/ocr-utils/.install.jar
modules/ocr-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/ocr-utils/.install
modules/ocr-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/ocr-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/ocr-utils/.install-doc.jar
modules/ocr-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/ocr-utils/.install-xprocdoc.jar
modules/ocr-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/ocr-utils/.install-javadoc.jar
modules/ocr-utils/.install-javadoc.jar : %/.install-javadoc.jar : %/.install-doc

.SECONDARY : modules/ocr-utils/.install-doc
modules/ocr-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/ocr-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/ocr-utils/.compile-dependencies modules/ocr-utils/.test-dependencies
modules/ocr-utils/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/ocr-utils/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/ocr-utils/1.1.1/ocr-utils-1.1.1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/ocr-utils/1.1.1/ocr-utils-1.1.1-% : modules/ocr-utils/.release
	+//

.SECONDARY : modules/ocr-utils/.release
modules/ocr-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("ocr-utils");

modules/ocr-utils/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/ocr-utils/.clean
.PHONY : modules/ocr-utils/.clean
modules/ocr-utils/.clean :
	rm("modules/ocr-utils/target");
