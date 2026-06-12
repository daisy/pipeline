modules/liblouis-utils/VERSION := 6.5.1-SNAPSHOT

$(TARGET_DIR)/state/modules/liblouis-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/liblouis-utils/modified-since-release_ : modules/liblouis-utils/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/liblouis-utils/.test
modules/liblouis-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/liblouis-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/liblouis-utils/6.5.1-SNAPSHOT/liblouis-utils-6.5.1-SNAPSHOT.pom : modules/liblouis-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/liblouis-utils/6.5.1-SNAPSHOT/liblouis-utils-6.5.1-SNAPSHOT% : modules/liblouis-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/liblouis-utils/.install.pom
modules/liblouis-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/liblouis-utils");

modules/liblouis-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/liblouis-utils/.install.jar
modules/liblouis-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/liblouis-utils/.install
modules/liblouis-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/liblouis-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/liblouis-utils/.install-doc.jar
modules/liblouis-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/liblouis-utils/.install-xprocdoc.jar
modules/liblouis-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/liblouis-utils/.install-javadoc.jar
modules/liblouis-utils/.install-javadoc.jar : %/.install-javadoc.jar : %/.install-doc

.SECONDARY : modules/liblouis-utils/.install-doc
modules/liblouis-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/liblouis-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/liblouis-utils/.compile-dependencies modules/liblouis-utils/.test-dependencies
modules/liblouis-utils/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/liblouis-utils/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/liblouis-utils/6.5.1/liblouis-utils-6.5.1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/liblouis-utils/6.5.1/liblouis-utils-6.5.1-% : modules/liblouis-utils/.release
	+//

.SECONDARY : modules/liblouis-utils/.release
modules/liblouis-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("liblouis-utils");

modules/liblouis-utils/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/liblouis-utils/.clean
.PHONY : modules/liblouis-utils/.clean
modules/liblouis-utils/.clean :
	rm("modules/liblouis-utils/target");
