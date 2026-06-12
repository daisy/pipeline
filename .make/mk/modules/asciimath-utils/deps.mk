modules/asciimath-utils/VERSION := 2.0.3-SNAPSHOT

$(TARGET_DIR)/state/modules/asciimath-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/asciimath-utils/modified-since-release_ : modules/asciimath-utils/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/asciimath-utils/.test
modules/asciimath-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/asciimath-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/asciimath-utils/2.0.3-SNAPSHOT/asciimath-utils-2.0.3-SNAPSHOT.pom : modules/asciimath-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/asciimath-utils/2.0.3-SNAPSHOT/asciimath-utils-2.0.3-SNAPSHOT% : modules/asciimath-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/asciimath-utils/.install.pom
modules/asciimath-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/asciimath-utils");

modules/asciimath-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/asciimath-utils/.install.jar
modules/asciimath-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/asciimath-utils/.install
modules/asciimath-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/asciimath-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/asciimath-utils/.install-doc.jar
modules/asciimath-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/asciimath-utils/.install-xprocdoc.jar
modules/asciimath-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/asciimath-utils/.install-doc
modules/asciimath-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/asciimath-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/asciimath-utils/.compile-dependencies modules/asciimath-utils/.test-dependencies
modules/asciimath-utils/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/asciimath-utils/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/asciimath-utils/2.0.3/asciimath-utils-2.0.3.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/asciimath-utils/2.0.3/asciimath-utils-2.0.3-% : modules/asciimath-utils/.release
	+//

.SECONDARY : modules/asciimath-utils/.release
modules/asciimath-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("asciimath-utils");

modules/asciimath-utils/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/asciimath-utils/.clean
.PHONY : modules/asciimath-utils/.clean
modules/asciimath-utils/.clean :
	rm("modules/asciimath-utils/target");
