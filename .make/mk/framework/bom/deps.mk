framework/bom/VERSION := 1.15.7-SNAPSHOT

$(TARGET_DIR)/state/framework/bom/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : framework/bom/.test
framework/bom/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

framework/bom/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-bom/1.15.7-SNAPSHOT/framework-bom-1.15.7-SNAPSHOT.pom : framework/bom/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-bom/1.15.7-SNAPSHOT/framework-bom-1.15.7-SNAPSHOT% : framework/bom/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : framework/bom/.install.pom
framework/bom/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("$(MY_DIR)/pom/framework/bom");

framework/bom/.install.pom : %/.install.pom : $(MY_DIR)/pom/%/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : framework/bom/.install-doc
framework/bom/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

framework/bom/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : framework/bom/.compile-dependencies framework/bom/.test-dependencies
framework/bom/.compile-dependencies :
framework/bom/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-bom/1.15.7/framework-bom-1.15.7.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-bom/1.15.7/framework-bom-1.15.7-% : framework/bom/.release
	+//

.SECONDARY : framework/bom/.release
framework/bom/.release : framework/.release
	+$(EVAL) mvn.releaseModulesInDir("framework").apply("bom");

framework/bom/.release :

clean : framework/bom/.clean
.PHONY : framework/bom/.clean
framework/bom/.clean :
	rm("framework/bom/target");
