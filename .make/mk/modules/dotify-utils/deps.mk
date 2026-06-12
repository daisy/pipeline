modules/dotify-utils/VERSION := 6.6.1-SNAPSHOT

$(TARGET_DIR)/state/modules/dotify-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/dotify-utils/modified-since-release_ : modules/dotify-utils/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/dotify-utils/.test
modules/dotify-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/dotify-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/dotify-utils/6.6.1-SNAPSHOT/dotify-utils-6.6.1-SNAPSHOT.pom : modules/dotify-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/dotify-utils/6.6.1-SNAPSHOT/dotify-utils-6.6.1-SNAPSHOT% : modules/dotify-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/dotify-utils/.install.pom
modules/dotify-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/dotify-utils");

modules/dotify-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/dotify-utils/.install.jar
modules/dotify-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/dotify-utils/.install
modules/dotify-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/dotify-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/dotify-utils/.install-doc.jar
modules/dotify-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/dotify-utils/.install-xprocdoc.jar
modules/dotify-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/dotify-utils/.install-javadoc.jar
modules/dotify-utils/.install-javadoc.jar : %/.install-javadoc.jar : %/.install-doc

.SECONDARY : modules/dotify-utils/.install-doc
modules/dotify-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/dotify-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/dotify-utils/.compile-dependencies modules/dotify-utils/.test-dependencies
modules/dotify-utils/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/dotify-utils/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/dotify-utils/6.6.1/dotify-utils-6.6.1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/dotify-utils/6.6.1/dotify-utils-6.6.1-% : modules/dotify-utils/.release
	+//

.SECONDARY : modules/dotify-utils/.release
modules/dotify-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("dotify-utils");

modules/dotify-utils/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/dotify-utils/.clean
.PHONY : modules/dotify-utils/.clean
modules/dotify-utils/.clean :
	rm("modules/dotify-utils/target");
