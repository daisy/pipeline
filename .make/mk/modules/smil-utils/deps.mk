modules/smil-utils/VERSION := 4.0.6-SNAPSHOT

$(TARGET_DIR)/state/modules/smil-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/smil-utils/modified-since-release_ : modules/smil-utils/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/smil-utils/.test
modules/smil-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/smil-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/smil-utils/4.0.6-SNAPSHOT/smil-utils-4.0.6-SNAPSHOT.pom : modules/smil-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/smil-utils/4.0.6-SNAPSHOT/smil-utils-4.0.6-SNAPSHOT% : modules/smil-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/smil-utils/.install.pom
modules/smil-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/smil-utils");

modules/smil-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/smil-utils/.install.jar
modules/smil-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/smil-utils/.install
modules/smil-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/smil-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/smil-utils/.install-doc.jar
modules/smil-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/smil-utils/.install-xprocdoc.jar
modules/smil-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/smil-utils/.install-doc
modules/smil-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/smil-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/smil-utils/.compile-dependencies modules/smil-utils/.test-dependencies
modules/smil-utils/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/smil-utils/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/smil-utils/4.0.6/smil-utils-4.0.6.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/smil-utils/4.0.6/smil-utils-4.0.6-% : modules/smil-utils/.release
	+//

.SECONDARY : modules/smil-utils/.release
modules/smil-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("smil-utils");

modules/smil-utils/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/smil-utils/.clean
.PHONY : modules/smil-utils/.clean
modules/smil-utils/.clean :
	rm("modules/smil-utils/target");
