assembly/VERSION := 1.15.6-SNAPSHOT

$(TARGET_DIR)/state/assembly/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/assembly/modified-since-release_ : assembly/pom.xml $(TARGET_DIR)/state/modules/word-to-dtbook/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : assembly/.test
assembly/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

assembly/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/1.15.6-SNAPSHOT/assembly-1.15.6-SNAPSHOT.pom : assembly/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/1.15.6-SNAPSHOT/assembly-1.15.6-SNAPSHOT% : assembly/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : assembly/.install.pom
assembly/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("assembly");

assembly/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : assembly/.install.jar
assembly/.install.jar : %/.install.jar : %/.install

.SECONDARY : assembly/.install
assembly/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

assembly/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : assembly/.install-doc
assembly/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

assembly/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : assembly/.compile-dependencies assembly/.test-dependencies
assembly/.compile-dependencies : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-bom/1.15.6-SNAPSHOT/modules-bom-1.15.6-SNAPSHOT.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/word-to-dtbook/1.1.4-SNAPSHOT/word-to-dtbook-1.1.4-SNAPSHOT.jar
assembly/.test-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-bom/1.15.6-SNAPSHOT/modules-bom-1.15.6-SNAPSHOT.pom

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/1.15.6/assembly-1.15.6.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/1.15.6/assembly-1.15.6-% : assembly/.release
	+//

.SECONDARY : assembly/.release
assembly/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

assembly/.release : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-bom/1.15.6/modules-bom-1.15.6.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/word-to-dtbook/1.1.4/word-to-dtbook-1.1.4.jar

clean : assembly/.clean
.PHONY : assembly/.clean
assembly/.clean :
	rm("assembly/target");
