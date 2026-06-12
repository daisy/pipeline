modules/image-utils/VERSION := 1.0.10-SNAPSHOT

$(TARGET_DIR)/state/modules/image-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/image-utils/modified-since-release_ : modules/image-utils/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/image-utils/.test
modules/image-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/image-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/image-utils/1.0.10-SNAPSHOT/image-utils-1.0.10-SNAPSHOT.pom : modules/image-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/image-utils/1.0.10-SNAPSHOT/image-utils-1.0.10-SNAPSHOT% : modules/image-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/image-utils/.install.pom
modules/image-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/image-utils");

modules/image-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/image-utils/.install.jar
modules/image-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/image-utils/.install
modules/image-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/image-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/image-utils/.install-doc.jar
modules/image-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/image-utils/.install-xprocdoc.jar
modules/image-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/image-utils/.install-doc
modules/image-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/image-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/image-utils/.compile-dependencies modules/image-utils/.test-dependencies
modules/image-utils/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/image-utils/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/image-utils/1.0.10/image-utils-1.0.10.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/image-utils/1.0.10/image-utils-1.0.10-% : modules/image-utils/.release
	+//

.SECONDARY : modules/image-utils/.release
modules/image-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("image-utils");

modules/image-utils/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/image-utils/.clean
.PHONY : modules/image-utils/.clean
modules/image-utils/.clean :
	rm("modules/image-utils/target");
