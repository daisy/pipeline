utils/xprocspec/VERSION := 1.4.4-SNAPSHOT

$(TARGET_DIR)/state/utils/xprocspec/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/utils/xprocspec/modified-since-release_ : utils/xprocspec/pom.xml \
	$(TARGET_DIR)/state/utils/xproc-maven-plugin/xproc-maven-plugin/modified-since-release \
	$(TARGET_DIR)/state/utils/xprocspec/modified-since-release \
	$(TARGET_DIR)/state/utils/xproc-maven-plugin/xproc-engine-calabash/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : utils/xprocspec/.test
utils/xprocspec/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

utils/xprocspec/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/xprocspec/xprocspec/1.4.4-SNAPSHOT/xprocspec-1.4.4-SNAPSHOT.pom : utils/xprocspec/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/xprocspec/xprocspec/1.4.4-SNAPSHOT/xprocspec-1.4.4-SNAPSHOT% : utils/xprocspec/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : utils/xprocspec/.install.pom
utils/xprocspec/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("utils/xprocspec");

utils/xprocspec/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/xprocspec/.install.jar
utils/xprocspec/.install.jar : %/.install.jar : %/.install

.SECONDARY : utils/xprocspec/.install
utils/xprocspec/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

utils/xprocspec/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/xprocspec/.install-doc
utils/xprocspec/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

utils/xprocspec/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : utils/xprocspec/.compile-dependencies utils/xprocspec/.test-dependencies
utils/xprocspec/.compile-dependencies : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-maven-plugin/1.0.4-SNAPSHOT/xproc-maven-plugin-1.0.4-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/xprocspec/xprocspec/1.4.4-SNAPSHOT/xprocspec-1.4.4-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-calabash/1.2.1-SNAPSHOT/xproc-engine-calabash-1.2.1-SNAPSHOT.jar
utils/xprocspec/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/xprocspec/xprocspec/1.4.4/xprocspec-1.4.4.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/xprocspec/xprocspec/1.4.4/xprocspec-1.4.4-% : utils/xprocspec/.release
	+//

.SECONDARY : utils/xprocspec/.release
utils/xprocspec/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

utils/xprocspec/.release : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-maven-plugin/1.0.4/xproc-maven-plugin-1.0.4.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/xprocspec/xprocspec/1.4.4/xprocspec-1.4.4.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-calabash/1.2.1/xproc-engine-calabash-1.2.1.jar

clean : utils/xprocspec/.clean
.PHONY : utils/xprocspec/.clean
utils/xprocspec/.clean :
	rm("utils/xprocspec/target");
