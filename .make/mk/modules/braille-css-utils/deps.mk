modules/braille-css-utils/VERSION := 5.0.3-SNAPSHOT

$(TARGET_DIR)/state/modules/braille-css-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/braille-css-utils/modified-since-release_ : modules/braille-css-utils/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/braille-css-utils/.test
modules/braille-css-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/braille-css-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-css-utils/5.0.3-SNAPSHOT/braille-css-utils-5.0.3-SNAPSHOT.pom : modules/braille-css-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-css-utils/5.0.3-SNAPSHOT/braille-css-utils-5.0.3-SNAPSHOT% : modules/braille-css-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/braille-css-utils/.install.pom
modules/braille-css-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/braille-css-utils");

modules/braille-css-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/braille-css-utils/.install.jar
modules/braille-css-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/braille-css-utils/.install
modules/braille-css-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/braille-css-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/braille-css-utils/.install-doc.jar
modules/braille-css-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/braille-css-utils/.install-xprocdoc.jar
modules/braille-css-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/braille-css-utils/.install-javadoc.jar
modules/braille-css-utils/.install-javadoc.jar : %/.install-javadoc.jar : %/.install-doc

.SECONDARY : modules/braille-css-utils/.install-doc
modules/braille-css-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/braille-css-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/braille-css-utils/.compile-dependencies modules/braille-css-utils/.test-dependencies
modules/braille-css-utils/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/braille-css-utils/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-css-utils/5.0.3/braille-css-utils-5.0.3.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-css-utils/5.0.3/braille-css-utils-5.0.3-% : modules/braille-css-utils/.release
	+//

.SECONDARY : modules/braille-css-utils/.release
modules/braille-css-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("braille-css-utils");

modules/braille-css-utils/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/braille-css-utils/.clean
.PHONY : modules/braille-css-utils/.clean
modules/braille-css-utils/.clean :
	rm("modules/braille-css-utils/target");
