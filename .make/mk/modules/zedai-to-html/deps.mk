modules/zedai-to-html/VERSION := 2.6.4-SNAPSHOT

$(TARGET_DIR)/state/modules/zedai-to-html/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/zedai-to-html/modified-since-release_ : modules/zedai-to-html/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/zedai-to-html/.test
modules/zedai-to-html/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/zedai-to-html/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-to-html/2.6.4-SNAPSHOT/zedai-to-html-2.6.4-SNAPSHOT.pom : modules/zedai-to-html/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-to-html/2.6.4-SNAPSHOT/zedai-to-html-2.6.4-SNAPSHOT% : modules/zedai-to-html/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/zedai-to-html/.install.pom
modules/zedai-to-html/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/zedai-to-html");

modules/zedai-to-html/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/zedai-to-html/.install.jar
modules/zedai-to-html/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/zedai-to-html/.install
modules/zedai-to-html/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/zedai-to-html/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/zedai-to-html/.install-doc.jar
modules/zedai-to-html/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/zedai-to-html/.install-xprocdoc.jar
modules/zedai-to-html/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/zedai-to-html/.install-doc
modules/zedai-to-html/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/zedai-to-html/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/zedai-to-html/.compile-dependencies modules/zedai-to-html/.test-dependencies
modules/zedai-to-html/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/zedai-to-html/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-to-html/2.6.4/zedai-to-html-2.6.4.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-to-html/2.6.4/zedai-to-html-2.6.4-% : modules/zedai-to-html/.release
	+//

.SECONDARY : modules/zedai-to-html/.release
modules/zedai-to-html/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("zedai-to-html");

modules/zedai-to-html/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/zedai-to-html/.clean
.PHONY : modules/zedai-to-html/.clean
modules/zedai-to-html/.clean :
	rm("modules/zedai-to-html/target");
