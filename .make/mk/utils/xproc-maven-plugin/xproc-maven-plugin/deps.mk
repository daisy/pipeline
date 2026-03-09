utils/xproc-maven-plugin/xproc-maven-plugin/VERSION := 1.0.4-SNAPSHOT

$(TARGET_DIR)/state/utils/xproc-maven-plugin/xproc-maven-plugin/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/utils/xproc-maven-plugin/xproc-maven-plugin/modified-since-release_ : utils/xproc-maven-plugin/xproc-maven-plugin/pom.xml $(TARGET_DIR)/state/utils/xproc-maven-plugin/xproc-engine-calabash/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : utils/xproc-maven-plugin/xproc-maven-plugin/.test
utils/xproc-maven-plugin/xproc-maven-plugin/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

utils/xproc-maven-plugin/xproc-maven-plugin/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-maven-plugin/1.0.4-SNAPSHOT/xproc-maven-plugin-1.0.4-SNAPSHOT.pom : utils/xproc-maven-plugin/xproc-maven-plugin/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-maven-plugin/1.0.4-SNAPSHOT/xproc-maven-plugin-1.0.4-SNAPSHOT% : utils/xproc-maven-plugin/xproc-maven-plugin/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : utils/xproc-maven-plugin/xproc-maven-plugin/.install.pom
utils/xproc-maven-plugin/xproc-maven-plugin/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("utils/xproc-maven-plugin/xproc-maven-plugin");

utils/xproc-maven-plugin/xproc-maven-plugin/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/xproc-maven-plugin/xproc-maven-plugin/.install.jar
utils/xproc-maven-plugin/xproc-maven-plugin/.install.jar : %/.install.jar : %/.install

.SECONDARY : utils/xproc-maven-plugin/xproc-maven-plugin/.install
utils/xproc-maven-plugin/xproc-maven-plugin/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

utils/xproc-maven-plugin/xproc-maven-plugin/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/xproc-maven-plugin/xproc-maven-plugin/.install-doc
utils/xproc-maven-plugin/xproc-maven-plugin/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

utils/xproc-maven-plugin/xproc-maven-plugin/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : utils/xproc-maven-plugin/xproc-maven-plugin/.compile-dependencies utils/xproc-maven-plugin/xproc-maven-plugin/.test-dependencies
utils/xproc-maven-plugin/xproc-maven-plugin/.compile-dependencies :
utils/xproc-maven-plugin/xproc-maven-plugin/.test-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-calabash/1.2.1-SNAPSHOT/xproc-engine-calabash-1.2.1-SNAPSHOT.jar

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-maven-plugin/1.0.4/xproc-maven-plugin-1.0.4.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-maven-plugin/1.0.4/xproc-maven-plugin-1.0.4-% : utils/xproc-maven-plugin/xproc-maven-plugin/.release
	+//

.SECONDARY : utils/xproc-maven-plugin/xproc-maven-plugin/.release
utils/xproc-maven-plugin/xproc-maven-plugin/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

utils/xproc-maven-plugin/xproc-maven-plugin/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-calabash/1.2.1/xproc-engine-calabash-1.2.1.jar

clean : utils/xproc-maven-plugin/xproc-maven-plugin/.clean
.PHONY : utils/xproc-maven-plugin/xproc-maven-plugin/.clean
utils/xproc-maven-plugin/xproc-maven-plugin/.clean :
	rm("utils/xproc-maven-plugin/xproc-maven-plugin/target");
