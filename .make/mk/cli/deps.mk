cli/VERSION := 2.3.1-SNAPSHOT

$(TARGET_DIR)/state/cli/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : cli/.test
cli/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

cli/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/cli/2.3.1-SNAPSHOT/cli-2.3.1-SNAPSHOT.pom : cli/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/cli/2.3.1-SNAPSHOT/cli-2.3.1-SNAPSHOT% : cli/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : cli/.install.pom
cli/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("cli");

cli/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : cli/.install-doc
cli/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

cli/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : cli/.compile-dependencies cli/.test-dependencies
cli/.compile-dependencies :
cli/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/cli/2.3.1/cli-2.3.1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/cli/2.3.1/cli-2.3.1-% : cli/.release
	+//

.SECONDARY : cli/.release
cli/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

cli/.release :

clean : cli/.clean
.PHONY : cli/.clean
cli/.clean :
	rm("cli/target");
