framework/utils/xproc-engine-daisy-pipeline-logging/VERSION := 1.0.1-SNAPSHOT

$(TARGET_DIR)/state/framework/utils/xproc-engine-daisy-pipeline-logging/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/framework/utils/xproc-engine-daisy-pipeline-logging/modified-since-release_ : framework/utils/xproc-engine-daisy-pipeline-logging/pom.xml $(TARGET_DIR)/state/framework/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : framework/utils/xproc-engine-daisy-pipeline-logging/.test
framework/utils/xproc-engine-daisy-pipeline-logging/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

framework/utils/xproc-engine-daisy-pipeline-logging/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-daisy-pipeline-logging/1.0.1-SNAPSHOT/xproc-engine-daisy-pipeline-logging-1.0.1-SNAPSHOT.pom : framework/utils/xproc-engine-daisy-pipeline-logging/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-daisy-pipeline-logging/1.0.1-SNAPSHOT/xproc-engine-daisy-pipeline-logging-1.0.1-SNAPSHOT% : framework/utils/xproc-engine-daisy-pipeline-logging/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : framework/utils/xproc-engine-daisy-pipeline-logging/.install.pom
framework/utils/xproc-engine-daisy-pipeline-logging/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("framework/utils/xproc-engine-daisy-pipeline-logging");

framework/utils/xproc-engine-daisy-pipeline-logging/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : framework/utils/xproc-engine-daisy-pipeline-logging/.install.jar
framework/utils/xproc-engine-daisy-pipeline-logging/.install.jar : %/.install.jar : %/.install

.SECONDARY : framework/utils/xproc-engine-daisy-pipeline-logging/.install
framework/utils/xproc-engine-daisy-pipeline-logging/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

framework/utils/xproc-engine-daisy-pipeline-logging/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : framework/utils/xproc-engine-daisy-pipeline-logging/.install-doc
framework/utils/xproc-engine-daisy-pipeline-logging/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

framework/utils/xproc-engine-daisy-pipeline-logging/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : framework/utils/xproc-engine-daisy-pipeline-logging/.compile-dependencies framework/utils/xproc-engine-daisy-pipeline-logging/.test-dependencies
framework/utils/xproc-engine-daisy-pipeline-logging/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-parent/1.15.7-SNAPSHOT/framework-parent-1.15.7-SNAPSHOT.pom
framework/utils/xproc-engine-daisy-pipeline-logging/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-daisy-pipeline-logging/1.0.1/xproc-engine-daisy-pipeline-logging-1.0.1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-daisy-pipeline-logging/1.0.1/xproc-engine-daisy-pipeline-logging-1.0.1-% : framework/utils/xproc-engine-daisy-pipeline-logging/.release
	+//

.SECONDARY : framework/utils/xproc-engine-daisy-pipeline-logging/.release
framework/utils/xproc-engine-daisy-pipeline-logging/.release : framework/.release
	+$(EVAL) mvn.releaseModulesInDir("framework").apply("utils/xproc-engine-daisy-pipeline-logging");

framework/utils/xproc-engine-daisy-pipeline-logging/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-parent/1.15.7/framework-parent-1.15.7.pom

clean : framework/utils/xproc-engine-daisy-pipeline-logging/.clean
.PHONY : framework/utils/xproc-engine-daisy-pipeline-logging/.clean
framework/utils/xproc-engine-daisy-pipeline-logging/.clean :
	rm("framework/utils/xproc-engine-daisy-pipeline-logging/target");
