framework/pipeline1-adapter/VERSION := 1.1.3-SNAPSHOT

$(TARGET_DIR)/state/framework/pipeline1-adapter/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/framework/pipeline1-adapter/modified-since-release_ : framework/pipeline1-adapter/pom.xml \
	$(TARGET_DIR)/state/framework/parent/modified-since-release \
	$(TARGET_DIR)/state/framework/common-utils/modified-since-release \
	$(TARGET_DIR)/state/framework/framework-core/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : framework/pipeline1-adapter/.test
framework/pipeline1-adapter/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

framework/pipeline1-adapter/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/pipeline1-adapter/1.1.3-SNAPSHOT/pipeline1-adapter-1.1.3-SNAPSHOT.pom : framework/pipeline1-adapter/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/pipeline1-adapter/1.1.3-SNAPSHOT/pipeline1-adapter-1.1.3-SNAPSHOT% : framework/pipeline1-adapter/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : framework/pipeline1-adapter/.install.pom
framework/pipeline1-adapter/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("framework/pipeline1-adapter");

framework/pipeline1-adapter/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : framework/pipeline1-adapter/.install.jar
framework/pipeline1-adapter/.install.jar : %/.install.jar : %/.install

.SECONDARY : framework/pipeline1-adapter/.install
framework/pipeline1-adapter/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

framework/pipeline1-adapter/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : framework/pipeline1-adapter/.install-doc
framework/pipeline1-adapter/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

framework/pipeline1-adapter/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : framework/pipeline1-adapter/.compile-dependencies framework/pipeline1-adapter/.test-dependencies
framework/pipeline1-adapter/.compile-dependencies : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-parent/1.15.7-SNAPSHOT/framework-parent-1.15.7-SNAPSHOT.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1-SNAPSHOT/common-utils-6.6.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.0.1-SNAPSHOT/framework-core-12.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/xproc-api/8.1.1-SNAPSHOT/xproc-api-8.1.1-SNAPSHOT.jar
framework/pipeline1-adapter/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/pipeline1-adapter/1.1.3/pipeline1-adapter-1.1.3.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/pipeline1-adapter/1.1.3/pipeline1-adapter-1.1.3-% : framework/pipeline1-adapter/.release
	+//

.SECONDARY : framework/pipeline1-adapter/.release
framework/pipeline1-adapter/.release : framework/.release
	+$(EVAL) mvn.releaseModulesInDir("framework").apply("pipeline1-adapter");

framework/pipeline1-adapter/.release : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-parent/1.15.7/framework-parent-1.15.7.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1/common-utils-6.6.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.0.1/framework-core-12.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/xproc-api/8.1.1/xproc-api-8.1.1.jar

clean : framework/pipeline1-adapter/.clean
.PHONY : framework/pipeline1-adapter/.clean
framework/pipeline1-adapter/.clean :
	rm("framework/pipeline1-adapter/target");
