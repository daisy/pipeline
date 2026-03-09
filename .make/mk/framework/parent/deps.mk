framework/parent/VERSION := 1.15.7-SNAPSHOT

$(TARGET_DIR)/state/framework/parent/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : framework/parent/.test
framework/parent/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

framework/parent/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-parent/1.15.7-SNAPSHOT/framework-parent-1.15.7-SNAPSHOT.pom : framework/parent/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-parent/1.15.7-SNAPSHOT/framework-parent-1.15.7-SNAPSHOT% : framework/parent/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : framework/parent/.install.pom
framework/parent/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("framework/parent");

framework/parent/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : framework/parent/.install-doc
framework/parent/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

framework/parent/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : framework/parent/.compile-dependencies framework/parent/.test-dependencies
framework/parent/.compile-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-bom/1.15.7-SNAPSHOT/framework-bom-1.15.7-SNAPSHOT.pom
framework/parent/.test-dependencies : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-bom/1.15.7-SNAPSHOT/framework-bom-1.15.7-SNAPSHOT.pom

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-parent/1.15.7/framework-parent-1.15.7.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-parent/1.15.7/framework-parent-1.15.7-% : framework/parent/.release
	+//

.SECONDARY : framework/parent/.release
framework/parent/.release : framework/.release
	+$(EVAL) mvn.releaseModulesInDir("framework").apply("parent");

framework/parent/.release : $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-bom/1.15.7/framework-bom-1.15.7.pom

clean : framework/parent/.clean
.PHONY : framework/parent/.clean
framework/parent/.clean :
	rm("framework/parent/target");
