framework/persistence-mysql/VERSION := 2.0.2-SNAPSHOT

$(TARGET_DIR)/state/framework/persistence-mysql/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/framework/persistence-mysql/modified-since-release_ : framework/persistence-mysql/pom.xml $(TARGET_DIR)/state/framework/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : framework/persistence-mysql/.test
framework/persistence-mysql/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

framework/persistence-mysql/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/persistence-mysql/2.0.2-SNAPSHOT/persistence-mysql-2.0.2-SNAPSHOT.pom : framework/persistence-mysql/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/persistence-mysql/2.0.2-SNAPSHOT/persistence-mysql-2.0.2-SNAPSHOT% : framework/persistence-mysql/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : framework/persistence-mysql/.install.pom
framework/persistence-mysql/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("framework/persistence-mysql");

framework/persistence-mysql/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : framework/persistence-mysql/.install.jar
framework/persistence-mysql/.install.jar : %/.install.jar : %/.install

.SECONDARY : framework/persistence-mysql/.install
framework/persistence-mysql/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

framework/persistence-mysql/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : framework/persistence-mysql/.install-doc
framework/persistence-mysql/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

framework/persistence-mysql/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : framework/persistence-mysql/.compile-dependencies framework/persistence-mysql/.test-dependencies
framework/persistence-mysql/.compile-dependencies : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-parent/1.15.7-SNAPSHOT/framework-parent-1.15.7-SNAPSHOT.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1-SNAPSHOT/common-utils-6.6.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.0.1-SNAPSHOT/framework-core-12.0.1-SNAPSHOT.jar
framework/persistence-mysql/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/persistence-mysql/2.0.2/persistence-mysql-2.0.2.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/persistence-mysql/2.0.2/persistence-mysql-2.0.2-% : framework/persistence-mysql/.release
	+//

.SECONDARY : framework/persistence-mysql/.release
framework/persistence-mysql/.release : framework/.release
	+$(EVAL) mvn.releaseModulesInDir("framework").apply("persistence-mysql");

framework/persistence-mysql/.release : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-parent/1.15.7/framework-parent-1.15.7.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1/common-utils-6.6.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.0.1/framework-core-12.0.1.jar

clean : framework/persistence-mysql/.clean
.PHONY : framework/persistence-mysql/.clean
framework/persistence-mysql/.clean :
	rm("framework/persistence-mysql/target");
