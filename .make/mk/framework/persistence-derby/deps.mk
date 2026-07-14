framework/persistence-derby/VERSION := 2.0.12-SNAPSHOT

$(TARGET_DIR)/state/framework/persistence-derby/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/framework/persistence-derby/modified-since-release_ : framework/persistence-derby/pom.xml \
	$(TARGET_DIR)/state/framework/parent/modified-since-release \
	$(TARGET_DIR)/state/framework/common-utils/modified-since-release \
	$(TARGET_DIR)/state/utils/build-utils/modules-test-helper/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : framework/persistence-derby/.test
framework/persistence-derby/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

framework/persistence-derby/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/persistence-derby/2.0.12-SNAPSHOT/persistence-derby-2.0.12-SNAPSHOT.pom : framework/persistence-derby/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/persistence-derby/2.0.12-SNAPSHOT/persistence-derby-2.0.12-SNAPSHOT% : framework/persistence-derby/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : framework/persistence-derby/.install.pom
framework/persistence-derby/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("framework/persistence-derby");

framework/persistence-derby/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : framework/persistence-derby/.install.jar
framework/persistence-derby/.install.jar : %/.install.jar : %/.install

.SECONDARY : framework/persistence-derby/.install
framework/persistence-derby/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

framework/persistence-derby/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : framework/persistence-derby/.install-doc
framework/persistence-derby/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

framework/persistence-derby/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : framework/persistence-derby/.compile-dependencies framework/persistence-derby/.test-dependencies
framework/persistence-derby/.compile-dependencies : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-parent/1.15.7-SNAPSHOT/framework-parent-1.15.7-SNAPSHOT.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1-SNAPSHOT/common-utils-6.6.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/xproc-api/8.1.1-SNAPSHOT/xproc-api-8.1.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.0.1-SNAPSHOT/framework-core-12.0.1-SNAPSHOT.jar
framework/persistence-derby/.test-dependencies : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-test-helper/3.0.0-SNAPSHOT/modules-test-helper-3.0.0-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/saxon-adapter/5.8.2-SNAPSHOT/saxon-adapter-5.8.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1-SNAPSHOT/common-utils-6.6.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules-registry/5.0.2-SNAPSHOT/modules-registry-5.0.2-SNAPSHOT.jar

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/persistence-derby/2.0.12/persistence-derby-2.0.12.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/persistence-derby/2.0.12/persistence-derby-2.0.12-% : framework/persistence-derby/.release
	+//

.SECONDARY : framework/persistence-derby/.release
framework/persistence-derby/.release : framework/.release
	+$(EVAL) mvn.releaseModulesInDir("framework").apply("persistence-derby");

framework/persistence-derby/.release : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-parent/1.15.7/framework-parent-1.15.7.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1/common-utils-6.6.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/xproc-api/8.1.1/xproc-api-8.1.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.0.1/framework-core-12.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-test-helper/3.0.0/modules-test-helper-3.0.0.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/saxon-adapter/5.8.2/saxon-adapter-5.8.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules-registry/5.0.2/modules-registry-5.0.2.jar

clean : framework/persistence-derby/.clean
.PHONY : framework/persistence-derby/.clean
framework/persistence-derby/.clean :
	rm("framework/persistence-derby/target");
