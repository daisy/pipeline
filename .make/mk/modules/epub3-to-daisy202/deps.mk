modules/epub3-to-daisy202/VERSION := 2.2.13-SNAPSHOT

$(TARGET_DIR)/state/modules/epub3-to-daisy202/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/epub3-to-daisy202/modified-since-release_ : modules/epub3-to-daisy202/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/epub3-to-daisy202/.test
modules/epub3-to-daisy202/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/epub3-to-daisy202/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub3-to-daisy202/2.2.13-SNAPSHOT/epub3-to-daisy202-2.2.13-SNAPSHOT.pom : modules/epub3-to-daisy202/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub3-to-daisy202/2.2.13-SNAPSHOT/epub3-to-daisy202-2.2.13-SNAPSHOT% : modules/epub3-to-daisy202/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/epub3-to-daisy202/.install.pom
modules/epub3-to-daisy202/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/epub3-to-daisy202");

modules/epub3-to-daisy202/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/epub3-to-daisy202/.install.jar
modules/epub3-to-daisy202/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/epub3-to-daisy202/.install
modules/epub3-to-daisy202/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/epub3-to-daisy202/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/epub3-to-daisy202/.install-doc.jar
modules/epub3-to-daisy202/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/epub3-to-daisy202/.install-xprocdoc.jar
modules/epub3-to-daisy202/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/epub3-to-daisy202/.install-doc
modules/epub3-to-daisy202/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/epub3-to-daisy202/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/epub3-to-daisy202/.compile-dependencies modules/epub3-to-daisy202/.test-dependencies
modules/epub3-to-daisy202/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/epub3-to-daisy202/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub3-to-daisy202/2.2.13/epub3-to-daisy202-2.2.13.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub3-to-daisy202/2.2.13/epub3-to-daisy202-2.2.13-% : modules/epub3-to-daisy202/.release
	+//

.SECONDARY : modules/epub3-to-daisy202/.release
modules/epub3-to-daisy202/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("epub3-to-daisy202");

modules/epub3-to-daisy202/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/epub3-to-daisy202/.clean
.PHONY : modules/epub3-to-daisy202/.clean
modules/epub3-to-daisy202/.clean :
	rm("modules/epub3-to-daisy202/target");
