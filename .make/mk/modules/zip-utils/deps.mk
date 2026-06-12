modules/zip-utils/VERSION := 2.1.12-SNAPSHOT

$(TARGET_DIR)/state/modules/zip-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/zip-utils/modified-since-release_ : modules/zip-utils/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/zip-utils/.test
modules/zip-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/zip-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zip-utils/2.1.12-SNAPSHOT/zip-utils-2.1.12-SNAPSHOT.pom : modules/zip-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zip-utils/2.1.12-SNAPSHOT/zip-utils-2.1.12-SNAPSHOT% : modules/zip-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/zip-utils/.install.pom
modules/zip-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/zip-utils");

modules/zip-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/zip-utils/.install.jar
modules/zip-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/zip-utils/.install
modules/zip-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/zip-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/zip-utils/.install-doc.jar
modules/zip-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/zip-utils/.install-xprocdoc.jar
modules/zip-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/zip-utils/.install-doc
modules/zip-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/zip-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/zip-utils/.compile-dependencies modules/zip-utils/.test-dependencies
modules/zip-utils/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/zip-utils/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zip-utils/2.1.12/zip-utils-2.1.12.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zip-utils/2.1.12/zip-utils-2.1.12-% : modules/zip-utils/.release
	+//

.SECONDARY : modules/zip-utils/.release
modules/zip-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("zip-utils");

modules/zip-utils/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/zip-utils/.clean
.PHONY : modules/zip-utils/.clean
modules/zip-utils/.clean :
	rm("modules/zip-utils/target");
