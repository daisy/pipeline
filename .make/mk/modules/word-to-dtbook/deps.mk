modules/word-to-dtbook/VERSION := 1.1.4-SNAPSHOT

$(TARGET_DIR)/state/modules/word-to-dtbook/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/word-to-dtbook/modified-since-release_ : modules/word-to-dtbook/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/word-to-dtbook/.test
modules/word-to-dtbook/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/word-to-dtbook/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/word-to-dtbook/1.1.4-SNAPSHOT/word-to-dtbook-1.1.4-SNAPSHOT.pom : modules/word-to-dtbook/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/word-to-dtbook/1.1.4-SNAPSHOT/word-to-dtbook-1.1.4-SNAPSHOT% : modules/word-to-dtbook/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/word-to-dtbook/.install.pom
modules/word-to-dtbook/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/word-to-dtbook");

modules/word-to-dtbook/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/word-to-dtbook/.install.jar
modules/word-to-dtbook/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/word-to-dtbook/.install
modules/word-to-dtbook/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/word-to-dtbook/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/word-to-dtbook/.install-doc.jar
modules/word-to-dtbook/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/word-to-dtbook/.install-xprocdoc.jar
modules/word-to-dtbook/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/word-to-dtbook/.install-doc
modules/word-to-dtbook/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/word-to-dtbook/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/word-to-dtbook/.compile-dependencies modules/word-to-dtbook/.test-dependencies
modules/word-to-dtbook/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/word-to-dtbook/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/word-to-dtbook/1.1.4/word-to-dtbook-1.1.4.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/word-to-dtbook/1.1.4/word-to-dtbook-1.1.4-% : modules/word-to-dtbook/.release
	+//

.SECONDARY : modules/word-to-dtbook/.release
modules/word-to-dtbook/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("word-to-dtbook");

modules/word-to-dtbook/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/word-to-dtbook/.clean
.PHONY : modules/word-to-dtbook/.clean
modules/word-to-dtbook/.clean :
	rm("modules/word-to-dtbook/target");
