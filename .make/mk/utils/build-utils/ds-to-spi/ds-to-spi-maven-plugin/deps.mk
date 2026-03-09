utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/VERSION := 1.1.6-SNAPSHOT

$(TARGET_DIR)/state/utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/modified-since-release_ : utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/pom.xml $(TARGET_DIR)/state/utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.test
utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/ds-to-spi-maven-plugin/1.1.6-SNAPSHOT/ds-to-spi-maven-plugin-1.1.6-SNAPSHOT.pom : utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/ds-to-spi-maven-plugin/1.1.6-SNAPSHOT/ds-to-spi-maven-plugin-1.1.6-SNAPSHOT% : utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.install.pom
utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin");

utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.install.jar
utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.install.jar : %/.install.jar : %/.install

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.install
utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.install-doc
utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.compile-dependencies utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.test-dependencies
utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/ds-to-spi-annotations-processor/1.1.5-SNAPSHOT/ds-to-spi-annotations-processor-1.1.5-SNAPSHOT.jar
utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/ds-to-spi-maven-plugin/1.1.6/ds-to-spi-maven-plugin-1.1.6.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/ds-to-spi-maven-plugin/1.1.6/ds-to-spi-maven-plugin-1.1.6-% : utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.release
	+//

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.release
utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/ds-to-spi-annotations-processor/1.1.5/ds-to-spi-annotations-processor-1.1.5.jar

clean : utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.clean
.PHONY : utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.clean
utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/.clean :
	rm("utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/target");
