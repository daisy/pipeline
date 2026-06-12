modules/zedai-utils/VERSION := 1.3.3-SNAPSHOT

$(TARGET_DIR)/state/modules/zedai-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/zedai-utils/modified-since-release_ : modules/zedai-utils/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/zedai-utils/.test
modules/zedai-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/zedai-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-utils/1.3.3-SNAPSHOT/zedai-utils-1.3.3-SNAPSHOT.pom : modules/zedai-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-utils/1.3.3-SNAPSHOT/zedai-utils-1.3.3-SNAPSHOT% : modules/zedai-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/zedai-utils/.install.pom
modules/zedai-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/zedai-utils");

modules/zedai-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/zedai-utils/.install.jar
modules/zedai-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/zedai-utils/.install
modules/zedai-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/zedai-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/zedai-utils/.install-doc.jar
modules/zedai-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/zedai-utils/.install-xprocdoc.jar
modules/zedai-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/zedai-utils/.install-doc
modules/zedai-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/zedai-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/zedai-utils/.compile-dependencies modules/zedai-utils/.test-dependencies
modules/zedai-utils/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/zedai-utils/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-utils/1.3.3/zedai-utils-1.3.3.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-utils/1.3.3/zedai-utils-1.3.3-% : modules/zedai-utils/.release
	+//

.SECONDARY : modules/zedai-utils/.release
modules/zedai-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("zedai-utils");

modules/zedai-utils/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/zedai-utils/.clean
.PHONY : modules/zedai-utils/.clean
modules/zedai-utils/.clean :
	rm("modules/zedai-utils/target");
