framework/woodstox-osgi-adapter/VERSION := 2.1.1-SNAPSHOT

$(TARGET_DIR)/state/framework/woodstox-osgi-adapter/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/framework/woodstox-osgi-adapter/modified-since-release_ : framework/woodstox-osgi-adapter/pom.xml $(TARGET_DIR)/state/framework/parent/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : framework/woodstox-osgi-adapter/.test
framework/woodstox-osgi-adapter/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

framework/woodstox-osgi-adapter/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/woodstox-osgi-adapter/2.1.1-SNAPSHOT/woodstox-osgi-adapter-2.1.1-SNAPSHOT.pom : framework/woodstox-osgi-adapter/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/woodstox-osgi-adapter/2.1.1-SNAPSHOT/woodstox-osgi-adapter-2.1.1-SNAPSHOT% : framework/woodstox-osgi-adapter/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : framework/woodstox-osgi-adapter/.install.pom
framework/woodstox-osgi-adapter/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("framework/woodstox-osgi-adapter");

framework/woodstox-osgi-adapter/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : framework/woodstox-osgi-adapter/.install.jar
framework/woodstox-osgi-adapter/.install.jar : %/.install.jar : %/.install

.SECONDARY : framework/woodstox-osgi-adapter/.install
framework/woodstox-osgi-adapter/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

framework/woodstox-osgi-adapter/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : framework/woodstox-osgi-adapter/.install-doc
framework/woodstox-osgi-adapter/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

framework/woodstox-osgi-adapter/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : framework/woodstox-osgi-adapter/.compile-dependencies framework/woodstox-osgi-adapter/.test-dependencies
framework/woodstox-osgi-adapter/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-parent/1.15.7-SNAPSHOT/framework-parent-1.15.7-SNAPSHOT.pom
framework/woodstox-osgi-adapter/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/woodstox-osgi-adapter/2.1.1/woodstox-osgi-adapter-2.1.1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/woodstox-osgi-adapter/2.1.1/woodstox-osgi-adapter-2.1.1-% : framework/woodstox-osgi-adapter/.release
	+//

.SECONDARY : framework/woodstox-osgi-adapter/.release
framework/woodstox-osgi-adapter/.release : framework/.release
	+$(EVAL) mvn.releaseModulesInDir("framework").apply("woodstox-osgi-adapter");

framework/woodstox-osgi-adapter/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-parent/1.15.7/framework-parent-1.15.7.pom

clean : framework/woodstox-osgi-adapter/.clean
.PHONY : framework/woodstox-osgi-adapter/.clean
framework/woodstox-osgi-adapter/.clean :
	rm("framework/woodstox-osgi-adapter/target");
