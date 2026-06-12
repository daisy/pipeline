modules/daisy3-to-daisy202/VERSION := 2.1.10-SNAPSHOT

$(TARGET_DIR)/state/modules/daisy3-to-daisy202/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/daisy3-to-daisy202/modified-since-release_ : modules/daisy3-to-daisy202/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/daisy3-to-daisy202/.test
modules/daisy3-to-daisy202/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/daisy3-to-daisy202/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy3-to-daisy202/2.1.10-SNAPSHOT/daisy3-to-daisy202-2.1.10-SNAPSHOT.pom : modules/daisy3-to-daisy202/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy3-to-daisy202/2.1.10-SNAPSHOT/daisy3-to-daisy202-2.1.10-SNAPSHOT% : modules/daisy3-to-daisy202/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/daisy3-to-daisy202/.install.pom
modules/daisy3-to-daisy202/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/daisy3-to-daisy202");

modules/daisy3-to-daisy202/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/daisy3-to-daisy202/.install.jar
modules/daisy3-to-daisy202/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/daisy3-to-daisy202/.install
modules/daisy3-to-daisy202/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/daisy3-to-daisy202/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/daisy3-to-daisy202/.install-doc.jar
modules/daisy3-to-daisy202/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/daisy3-to-daisy202/.install-xprocdoc.jar
modules/daisy3-to-daisy202/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/daisy3-to-daisy202/.install-doc
modules/daisy3-to-daisy202/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/daisy3-to-daisy202/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/daisy3-to-daisy202/.compile-dependencies modules/daisy3-to-daisy202/.test-dependencies
modules/daisy3-to-daisy202/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/daisy3-to-daisy202/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy3-to-daisy202/2.1.10/daisy3-to-daisy202-2.1.10.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy3-to-daisy202/2.1.10/daisy3-to-daisy202-2.1.10-% : modules/daisy3-to-daisy202/.release
	+//

.SECONDARY : modules/daisy3-to-daisy202/.release
modules/daisy3-to-daisy202/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("daisy3-to-daisy202");

modules/daisy3-to-daisy202/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/daisy3-to-daisy202/.clean
.PHONY : modules/daisy3-to-daisy202/.clean
modules/daisy3-to-daisy202/.clean :
	rm("modules/daisy3-to-daisy202/target");
