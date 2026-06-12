modules/mediatype-utils/VERSION := 2.1.3-SNAPSHOT

$(TARGET_DIR)/state/modules/mediatype-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/mediatype-utils/modified-since-release_ : modules/mediatype-utils/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/mediatype-utils/.test
modules/mediatype-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/mediatype-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mediatype-utils/2.1.3-SNAPSHOT/mediatype-utils-2.1.3-SNAPSHOT.pom : modules/mediatype-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mediatype-utils/2.1.3-SNAPSHOT/mediatype-utils-2.1.3-SNAPSHOT% : modules/mediatype-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/mediatype-utils/.install.pom
modules/mediatype-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/mediatype-utils");

modules/mediatype-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/mediatype-utils/.install.jar
modules/mediatype-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/mediatype-utils/.install
modules/mediatype-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/mediatype-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/mediatype-utils/.install-doc.jar
modules/mediatype-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/mediatype-utils/.install-xprocdoc.jar
modules/mediatype-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/mediatype-utils/.install-doc
modules/mediatype-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/mediatype-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/mediatype-utils/.compile-dependencies modules/mediatype-utils/.test-dependencies
modules/mediatype-utils/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/mediatype-utils/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mediatype-utils/2.1.3/mediatype-utils-2.1.3.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mediatype-utils/2.1.3/mediatype-utils-2.1.3-% : modules/mediatype-utils/.release
	+//

.SECONDARY : modules/mediatype-utils/.release
modules/mediatype-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("mediatype-utils");

modules/mediatype-utils/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/mediatype-utils/.clean
.PHONY : modules/mediatype-utils/.clean
modules/mediatype-utils/.clean :
	rm("modules/mediatype-utils/target");
