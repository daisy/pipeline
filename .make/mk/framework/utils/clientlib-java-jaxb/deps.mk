framework/utils/clientlib-java-jaxb/VERSION := 3.1.1-SNAPSHOT

$(TARGET_DIR)/state/framework/utils/clientlib-java-jaxb/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/framework/utils/clientlib-java-jaxb/modified-since-release_ : framework/utils/clientlib-java-jaxb/pom.xml $(TARGET_DIR)/state/framework/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : framework/utils/clientlib-java-jaxb/.test
framework/utils/clientlib-java-jaxb/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

framework/utils/clientlib-java-jaxb/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/clientlib-java-jaxb/3.1.1-SNAPSHOT/clientlib-java-jaxb-3.1.1-SNAPSHOT.pom : framework/utils/clientlib-java-jaxb/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/clientlib-java-jaxb/3.1.1-SNAPSHOT/clientlib-java-jaxb-3.1.1-SNAPSHOT% : framework/utils/clientlib-java-jaxb/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : framework/utils/clientlib-java-jaxb/.install.pom
framework/utils/clientlib-java-jaxb/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("framework/utils/clientlib-java-jaxb");

framework/utils/clientlib-java-jaxb/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : framework/utils/clientlib-java-jaxb/.install.jar
framework/utils/clientlib-java-jaxb/.install.jar : %/.install.jar : %/.install

.SECONDARY : framework/utils/clientlib-java-jaxb/.install
framework/utils/clientlib-java-jaxb/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

framework/utils/clientlib-java-jaxb/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : framework/utils/clientlib-java-jaxb/.install-doc
framework/utils/clientlib-java-jaxb/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

framework/utils/clientlib-java-jaxb/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : framework/utils/clientlib-java-jaxb/.compile-dependencies framework/utils/clientlib-java-jaxb/.test-dependencies
framework/utils/clientlib-java-jaxb/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-parent/1.15.7-SNAPSHOT/framework-parent-1.15.7-SNAPSHOT.pom
framework/utils/clientlib-java-jaxb/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/clientlib-java-jaxb/3.1.1/clientlib-java-jaxb-3.1.1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/clientlib-java-jaxb/3.1.1/clientlib-java-jaxb-3.1.1-% : framework/utils/clientlib-java-jaxb/.release
	+//

.SECONDARY : framework/utils/clientlib-java-jaxb/.release
framework/utils/clientlib-java-jaxb/.release : framework/.release
	+$(EVAL) mvn.releaseModulesInDir("framework").apply("utils/clientlib-java-jaxb");

framework/utils/clientlib-java-jaxb/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-parent/1.15.7/framework-parent-1.15.7.pom

clean : framework/utils/clientlib-java-jaxb/.clean
.PHONY : framework/utils/clientlib-java-jaxb/.clean
framework/utils/clientlib-java-jaxb/.clean :
	rm("framework/utils/clientlib-java-jaxb/target");
