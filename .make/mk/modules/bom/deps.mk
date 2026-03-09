modules/bom/VERSION := 1.15.5-SNAPSHOT

$(TARGET_DIR)/state/modules/bom/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : modules/bom/.test
modules/bom/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/bom/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-bom/1.15.5-SNAPSHOT/modules-bom-1.15.5-SNAPSHOT.pom : modules/bom/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-bom/1.15.5-SNAPSHOT/modules-bom-1.15.5-SNAPSHOT% : modules/bom/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/bom/.install.pom
modules/bom/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("$(MY_DIR)/pom/modules/bom");

modules/bom/.install.pom : %/.install.pom : $(MY_DIR)/pom/%/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/bom/.install-doc
modules/bom/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/bom/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/bom/.compile-dependencies modules/bom/.test-dependencies
modules/bom/.compile-dependencies :
modules/bom/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-bom/1.15.5/modules-bom-1.15.5.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-bom/1.15.5/modules-bom-1.15.5-% : modules/bom/.release
	+//

.SECONDARY : modules/bom/.release
modules/bom/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("bom");

modules/bom/.release :

clean : modules/bom/.clean
.PHONY : modules/bom/.clean
modules/bom/.clean :
	rm("modules/bom/target");
