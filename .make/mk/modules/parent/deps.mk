modules/parent/VERSION := 1.15.5-SNAPSHOT

$(TARGET_DIR)/state/modules/parent/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : modules/parent/.test
modules/parent/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/parent/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.5-SNAPSHOT/modules-parent-1.15.5-SNAPSHOT.pom : modules/parent/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.5-SNAPSHOT/modules-parent-1.15.5-SNAPSHOT% : modules/parent/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/parent/.install.pom
modules/parent/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/parent");

modules/parent/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/parent/.install-doc
modules/parent/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/parent/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/parent/.compile-dependencies modules/parent/.test-dependencies
modules/parent/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-bom/1.15.5-SNAPSHOT/modules-bom-1.15.5-SNAPSHOT.pom
modules/parent/.test-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-bom/1.15.5-SNAPSHOT/modules-bom-1.15.5-SNAPSHOT.pom

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.5/modules-parent-1.15.5.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.5/modules-parent-1.15.5-% : modules/parent/.release
	+//

.SECONDARY : modules/parent/.release
modules/parent/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("parent");

modules/parent/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-bom/1.15.5/modules-bom-1.15.5.pom

clean : modules/parent/.clean
.PHONY : modules/parent/.clean
modules/parent/.clean :
	rm("modules/parent/target");
