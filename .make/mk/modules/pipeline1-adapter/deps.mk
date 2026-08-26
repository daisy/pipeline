modules/pipeline1-adapter/VERSION := 1.1.3-SNAPSHOT

$(TARGET_DIR)/state/modules/pipeline1-adapter/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/pipeline1-adapter/modified-since-release_ : modules/pipeline1-adapter/pom.xml \
	$(TARGET_DIR)/state/modules/parent/modified-since-release \
	$(TARGET_DIR)/state/framework/common-utils/modified-since-release \
	$(TARGET_DIR)/state/framework/framework-core/modified-since-release \
	$(TARGET_DIR)/state/framework/webservice/modified-since-release \
	$(TARGET_DIR)/state/framework/persistence-derby/modified-since-release \
	$(TARGET_DIR)/state/framework/calabash-adapter/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/pipeline1-adapter/.test
modules/pipeline1-adapter/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/pipeline1-adapter/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/pipeline1-adapter/1.1.3-SNAPSHOT/pipeline1-adapter-1.1.3-SNAPSHOT.pom : modules/pipeline1-adapter/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/pipeline1-adapter/1.1.3-SNAPSHOT/pipeline1-adapter-1.1.3-SNAPSHOT% : modules/pipeline1-adapter/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/pipeline1-adapter/.install.pom
modules/pipeline1-adapter/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/pipeline1-adapter");

modules/pipeline1-adapter/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/pipeline1-adapter/.install.jar
modules/pipeline1-adapter/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/pipeline1-adapter/.install
modules/pipeline1-adapter/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/pipeline1-adapter/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/pipeline1-adapter/.install-doc.jar
modules/pipeline1-adapter/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/pipeline1-adapter/.install-xprocdoc.jar
modules/pipeline1-adapter/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/pipeline1-adapter/.install-doc
modules/pipeline1-adapter/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/pipeline1-adapter/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/pipeline1-adapter/.compile-dependencies modules/pipeline1-adapter/.test-dependencies
modules/pipeline1-adapter/.compile-dependencies : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.5-SNAPSHOT/modules-parent-1.15.5-SNAPSHOT.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.7.1-SNAPSHOT/common-utils-6.7.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.1.1-SNAPSHOT/framework-core-12.1.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/xproc-api/8.1.2-SNAPSHOT/xproc-api-8.1.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/calabash-adapter/7.1.2-SNAPSHOT/calabash-adapter-7.1.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/saxon-adapter/5.8.3-SNAPSHOT/saxon-adapter-5.8.3-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules-registry/5.0.3-SNAPSHOT/modules-registry-5.0.3-SNAPSHOT.jar
modules/pipeline1-adapter/.test-dependencies : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/webservice/4.0.2-SNAPSHOT/webservice-4.0.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.7.1-SNAPSHOT/common-utils-6.7.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.1.1-SNAPSHOT/framework-core-12.1.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/xproc-api/8.1.2-SNAPSHOT/xproc-api-8.1.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/persistence-derby/2.0.13-SNAPSHOT/persistence-derby-2.0.13-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-persistence/2.1.14-SNAPSHOT/framework-persistence-2.1.14-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules-registry/5.0.3-SNAPSHOT/modules-registry-5.0.3-SNAPSHOT.jar

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/pipeline1-adapter/1.1.3/pipeline1-adapter-1.1.3.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/pipeline1-adapter/1.1.3/pipeline1-adapter-1.1.3-% : modules/pipeline1-adapter/.release
	+//

.SECONDARY : modules/pipeline1-adapter/.release
modules/pipeline1-adapter/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("pipeline1-adapter");

modules/pipeline1-adapter/.release : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.5/modules-parent-1.15.5.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.7.1/common-utils-6.7.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.1.1/framework-core-12.1.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/xproc-api/8.1.2/xproc-api-8.1.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/webservice/4.0.2/webservice-4.0.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/persistence-derby/2.0.13/persistence-derby-2.0.13.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-persistence/2.1.14/framework-persistence-2.1.14.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/calabash-adapter/7.1.2/calabash-adapter-7.1.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/saxon-adapter/5.8.3/saxon-adapter-5.8.3.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules-registry/5.0.3/modules-registry-5.0.3.jar

clean : modules/pipeline1-adapter/.clean
.PHONY : modules/pipeline1-adapter/.clean
modules/pipeline1-adapter/.clean :
	rm("modules/pipeline1-adapter/target");
