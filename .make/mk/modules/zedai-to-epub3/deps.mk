modules/zedai-to-epub3/VERSION := 2.8.1-SNAPSHOT

$(TARGET_DIR)/state/modules/zedai-to-epub3/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/zedai-to-epub3/modified-since-release_ : modules/zedai-to-epub3/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/zedai-to-epub3/.test
modules/zedai-to-epub3/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/zedai-to-epub3/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-to-epub3/2.8.1-SNAPSHOT/zedai-to-epub3-2.8.1-SNAPSHOT.pom : modules/zedai-to-epub3/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-to-epub3/2.8.1-SNAPSHOT/zedai-to-epub3-2.8.1-SNAPSHOT% : modules/zedai-to-epub3/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/zedai-to-epub3/.install.pom
modules/zedai-to-epub3/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/zedai-to-epub3");

modules/zedai-to-epub3/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/zedai-to-epub3/.install.jar
modules/zedai-to-epub3/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/zedai-to-epub3/.install
modules/zedai-to-epub3/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/zedai-to-epub3/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/zedai-to-epub3/.install-doc.jar
modules/zedai-to-epub3/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/zedai-to-epub3/.install-xprocdoc.jar
modules/zedai-to-epub3/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/zedai-to-epub3/.install-doc
modules/zedai-to-epub3/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/zedai-to-epub3/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/zedai-to-epub3/.compile-dependencies modules/zedai-to-epub3/.test-dependencies
modules/zedai-to-epub3/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/zedai-to-epub3/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-to-epub3/2.8.1/zedai-to-epub3-2.8.1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-to-epub3/2.8.1/zedai-to-epub3-2.8.1-% : modules/zedai-to-epub3/.release
	+//

.SECONDARY : modules/zedai-to-epub3/.release
modules/zedai-to-epub3/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("zedai-to-epub3");

modules/zedai-to-epub3/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/zedai-to-epub3/.clean
.PHONY : modules/zedai-to-epub3/.clean
modules/zedai-to-epub3/.clean :
	rm("modules/zedai-to-epub3/target");
