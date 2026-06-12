modules/libhyphen-utils/VERSION := 3.5.2-SNAPSHOT

$(TARGET_DIR)/state/modules/libhyphen-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/libhyphen-utils/modified-since-release_ : modules/libhyphen-utils/pom.xml $(TARGET_DIR)/state/modules/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/libhyphen-utils/.test
modules/libhyphen-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/libhyphen-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/libhyphen-utils/3.5.2-SNAPSHOT/libhyphen-utils-3.5.2-SNAPSHOT.pom : modules/libhyphen-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/libhyphen-utils/3.5.2-SNAPSHOT/libhyphen-utils-3.5.2-SNAPSHOT% : modules/libhyphen-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/libhyphen-utils/.install.pom
modules/libhyphen-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/libhyphen-utils");

modules/libhyphen-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/libhyphen-utils/.install.jar
modules/libhyphen-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/libhyphen-utils/.install
modules/libhyphen-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/libhyphen-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/libhyphen-utils/.install-doc.jar
modules/libhyphen-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/libhyphen-utils/.install-xprocdoc.jar
modules/libhyphen-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/libhyphen-utils/.install-javadoc.jar
modules/libhyphen-utils/.install-javadoc.jar : %/.install-javadoc.jar : %/.install-doc

.SECONDARY : modules/libhyphen-utils/.install-doc
modules/libhyphen-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/libhyphen-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/libhyphen-utils/.compile-dependencies modules/libhyphen-utils/.test-dependencies
modules/libhyphen-utils/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6-SNAPSHOT/modules-parent-1.15.6-SNAPSHOT.pom
modules/libhyphen-utils/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/libhyphen-utils/3.5.2/libhyphen-utils-3.5.2.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/libhyphen-utils/3.5.2/libhyphen-utils-3.5.2-% : modules/libhyphen-utils/.release
	+//

.SECONDARY : modules/libhyphen-utils/.release
modules/libhyphen-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("libhyphen-utils");

modules/libhyphen-utils/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.6/modules-parent-1.15.6.pom

clean : modules/libhyphen-utils/.clean
.PHONY : modules/libhyphen-utils/.clean
modules/libhyphen-utils/.clean :
	rm("modules/libhyphen-utils/target");
