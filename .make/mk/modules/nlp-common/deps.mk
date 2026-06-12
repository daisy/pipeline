modules/nlp-common/VERSION := 3.0.6-SNAPSHOT

$(TARGET_DIR)/state/modules/nlp-common/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/nlp-common/modified-since-release_ : modules/nlp-common/pom.xml \
	$(TARGET_DIR)/state/modules/parent/modified-since-release \
	$(TARGET_DIR)/state/modules/common-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/file-utils/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/nlp-common/.test
modules/nlp-common/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/nlp-common/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/nlp-common/3.0.6-SNAPSHOT/nlp-common-3.0.6-SNAPSHOT.pom : modules/nlp-common/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/nlp-common/3.0.6-SNAPSHOT/nlp-common-3.0.6-SNAPSHOT% : modules/nlp-common/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/nlp-common/.install.pom
modules/nlp-common/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/nlp-common");

modules/nlp-common/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/nlp-common/.install.jar
modules/nlp-common/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/nlp-common/.install
modules/nlp-common/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/nlp-common/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/nlp-common/.install-doc.jar
modules/nlp-common/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/nlp-common/.install-xprocdoc.jar
modules/nlp-common/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/nlp-common/.install-doc
modules/nlp-common/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/nlp-common/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/nlp-common/.compile-dependencies modules/nlp-common/.test-dependencies
modules/nlp-common/.compile-dependencies : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.5-SNAPSHOT/modules-parent-1.15.5-SNAPSHOT.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/common-utils/3.4.1-SNAPSHOT/common-utils-3.4.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/file-utils/5.0.1-SNAPSHOT/file-utils-5.0.1-SNAPSHOT.jar
modules/nlp-common/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/nlp-common/3.0.6/nlp-common-3.0.6.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/nlp-common/3.0.6/nlp-common-3.0.6-% : modules/nlp-common/.release
	+//

.SECONDARY : modules/nlp-common/.release
modules/nlp-common/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("nlp-common");

modules/nlp-common/.release : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.5/modules-parent-1.15.5.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/common-utils/3.4.1/common-utils-3.4.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/file-utils/5.0.1/file-utils-5.0.1.jar

clean : modules/nlp-common/.clean
.PHONY : modules/nlp-common/.clean
modules/nlp-common/.clean :
	rm("modules/nlp-common/target");
