modules/braille-common/VERSION := 7.0.2-SNAPSHOT

$(TARGET_DIR)/state/modules/braille-common/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/braille-common/modified-since-release_ : modules/braille-common/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/braille-common/.test
modules/braille-common/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/braille-common/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-common/7.0.2-SNAPSHOT/braille-common-7.0.2-SNAPSHOT.pom : modules/braille-common/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-common/7.0.2-SNAPSHOT/braille-common-7.0.2-SNAPSHOT% : modules/braille-common/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/braille-common/.install.pom
modules/braille-common/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/braille-common");

modules/braille-common/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/braille-common/.install.jar
modules/braille-common/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/braille-common/.install
modules/braille-common/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/braille-common/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/braille-common/.install-doc.jar
modules/braille-common/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/braille-common/.install-xprocdoc.jar
modules/braille-common/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/braille-common/.install-javadoc.jar
modules/braille-common/.install-javadoc.jar : %/.install-javadoc.jar : %/.install-doc

.SECONDARY : modules/braille-common/.install-doc
modules/braille-common/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/braille-common/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/braille-common/.compile-dependencies modules/braille-common/.test-dependencies
modules/braille-common/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/braille-common/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-common/7.0.2/braille-common-7.0.2.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-common/7.0.2/braille-common-7.0.2-% : modules/braille-common/.release
	+//

.SECONDARY : modules/braille-common/.release
modules/braille-common/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("braille-common");

modules/braille-common/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/braille-common/.clean
.PHONY : modules/braille-common/.clean
modules/braille-common/.clean :
	rm("modules/braille-common/target");
