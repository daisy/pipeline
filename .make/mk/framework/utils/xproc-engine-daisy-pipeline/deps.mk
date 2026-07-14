framework/utils/xproc-engine-daisy-pipeline/VERSION := 1.14.8-SNAPSHOT

$(TARGET_DIR)/state/framework/utils/xproc-engine-daisy-pipeline/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/framework/utils/xproc-engine-daisy-pipeline/modified-since-release_ : framework/utils/xproc-engine-daisy-pipeline/pom.xml \
	$(TARGET_DIR)/state/framework/parent/modified-since-release \
	$(TARGET_DIR)/state/framework/xproc-api/modified-since-release \
	$(TARGET_DIR)/state/framework/common-utils/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : framework/utils/xproc-engine-daisy-pipeline/.test
framework/utils/xproc-engine-daisy-pipeline/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

framework/utils/xproc-engine-daisy-pipeline/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-daisy-pipeline/1.14.8-SNAPSHOT/xproc-engine-daisy-pipeline-1.14.8-SNAPSHOT.pom : framework/utils/xproc-engine-daisy-pipeline/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-daisy-pipeline/1.14.8-SNAPSHOT/xproc-engine-daisy-pipeline-1.14.8-SNAPSHOT% : framework/utils/xproc-engine-daisy-pipeline/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : framework/utils/xproc-engine-daisy-pipeline/.install.pom
framework/utils/xproc-engine-daisy-pipeline/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("framework/utils/xproc-engine-daisy-pipeline");

framework/utils/xproc-engine-daisy-pipeline/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : framework/utils/xproc-engine-daisy-pipeline/.install.jar
framework/utils/xproc-engine-daisy-pipeline/.install.jar : %/.install.jar : %/.install

.SECONDARY : framework/utils/xproc-engine-daisy-pipeline/.install
framework/utils/xproc-engine-daisy-pipeline/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

framework/utils/xproc-engine-daisy-pipeline/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : framework/utils/xproc-engine-daisy-pipeline/.install-doc
framework/utils/xproc-engine-daisy-pipeline/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

framework/utils/xproc-engine-daisy-pipeline/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : framework/utils/xproc-engine-daisy-pipeline/.compile-dependencies framework/utils/xproc-engine-daisy-pipeline/.test-dependencies
framework/utils/xproc-engine-daisy-pipeline/.compile-dependencies : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-parent/1.15.7-SNAPSHOT/framework-parent-1.15.7-SNAPSHOT.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/xproc-api/8.1.1-SNAPSHOT/xproc-api-8.1.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1-SNAPSHOT/common-utils-6.6.1-SNAPSHOT.jar
framework/utils/xproc-engine-daisy-pipeline/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-daisy-pipeline/1.14.8/xproc-engine-daisy-pipeline-1.14.8.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-daisy-pipeline/1.14.8/xproc-engine-daisy-pipeline-1.14.8-% : framework/utils/xproc-engine-daisy-pipeline/.release
	+//

.SECONDARY : framework/utils/xproc-engine-daisy-pipeline/.release
framework/utils/xproc-engine-daisy-pipeline/.release : framework/.release
	+$(EVAL) mvn.releaseModulesInDir("framework").apply("utils/xproc-engine-daisy-pipeline");

framework/utils/xproc-engine-daisy-pipeline/.release : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-parent/1.15.7/framework-parent-1.15.7.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/xproc-api/8.1.1/xproc-api-8.1.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1/common-utils-6.6.1.jar

clean : framework/utils/xproc-engine-daisy-pipeline/.clean
.PHONY : framework/utils/xproc-engine-daisy-pipeline/.clean
framework/utils/xproc-engine-daisy-pipeline/.clean :
	rm("framework/utils/xproc-engine-daisy-pipeline/target");
