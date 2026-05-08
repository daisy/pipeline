framework/logging-appender/VERSION := 2.1.8-SNAPSHOT

$(TARGET_DIR)/state/framework/logging-appender/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/framework/logging-appender/modified-since-release_ : framework/logging-appender/pom.xml \
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

.SECONDARY : framework/logging-appender/.test
framework/logging-appender/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

framework/logging-appender/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/logging-appender/2.1.8-SNAPSHOT/logging-appender-2.1.8-SNAPSHOT.pom : framework/logging-appender/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/logging-appender/2.1.8-SNAPSHOT/logging-appender-2.1.8-SNAPSHOT% : framework/logging-appender/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : framework/logging-appender/.install.pom
framework/logging-appender/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("framework/logging-appender");

framework/logging-appender/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : framework/logging-appender/.install.jar
framework/logging-appender/.install.jar : %/.install.jar : %/.install

.SECONDARY : framework/logging-appender/.install
framework/logging-appender/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

framework/logging-appender/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : framework/logging-appender/.install-doc
framework/logging-appender/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

framework/logging-appender/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : framework/logging-appender/.compile-dependencies framework/logging-appender/.test-dependencies
framework/logging-appender/.compile-dependencies : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-parent/1.15.7-SNAPSHOT/framework-parent-1.15.7-SNAPSHOT.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1-SNAPSHOT/common-utils-6.6.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.0.1-SNAPSHOT/framework-core-12.0.1-SNAPSHOT.jar
framework/logging-appender/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/logging-appender/2.1.8/logging-appender-2.1.8.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/logging-appender/2.1.8/logging-appender-2.1.8-% : framework/logging-appender/.release
	+//

.SECONDARY : framework/logging-appender/.release
framework/logging-appender/.release : framework/.release
	+$(EVAL) mvn.releaseModulesInDir("framework").apply("logging-appender");

framework/logging-appender/.release : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-parent/1.15.7/framework-parent-1.15.7.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1/common-utils-6.6.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.0.1/framework-core-12.0.1.jar

clean : framework/logging-appender/.clean
.PHONY : framework/logging-appender/.clean
framework/logging-appender/.clean :
	rm("framework/logging-appender/target");
