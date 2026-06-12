utils/build-utils/modules-test-helper/VERSION := 3.0.0-SNAPSHOT

$(TARGET_DIR)/state/utils/build-utils/modules-test-helper/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : utils/build-utils/modules-test-helper/.test
utils/build-utils/modules-test-helper/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

utils/build-utils/modules-test-helper/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-test-helper/3.0.0-SNAPSHOT/modules-test-helper-3.0.0-SNAPSHOT.pom : utils/build-utils/modules-test-helper/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-test-helper/3.0.0-SNAPSHOT/modules-test-helper-3.0.0-SNAPSHOT% : utils/build-utils/modules-test-helper/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : utils/build-utils/modules-test-helper/.install.pom
utils/build-utils/modules-test-helper/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("utils/build-utils/modules-test-helper");

utils/build-utils/modules-test-helper/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/build-utils/modules-test-helper/.install.jar
utils/build-utils/modules-test-helper/.install.jar : %/.install.jar : %/.install

.SECONDARY : utils/build-utils/modules-test-helper/.install
utils/build-utils/modules-test-helper/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

utils/build-utils/modules-test-helper/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/build-utils/modules-test-helper/.install-doc
utils/build-utils/modules-test-helper/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

utils/build-utils/modules-test-helper/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : utils/build-utils/modules-test-helper/.compile-dependencies utils/build-utils/modules-test-helper/.test-dependencies
utils/build-utils/modules-test-helper/.compile-dependencies :
utils/build-utils/modules-test-helper/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-test-helper/3.0.0/modules-test-helper-3.0.0.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-test-helper/3.0.0/modules-test-helper-3.0.0-% : utils/build-utils/modules-test-helper/.release
	+//

.SECONDARY : utils/build-utils/modules-test-helper/.release
utils/build-utils/modules-test-helper/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

utils/build-utils/modules-test-helper/.release :

clean : utils/build-utils/modules-test-helper/.clean
.PHONY : utils/build-utils/modules-test-helper/.clean
utils/build-utils/modules-test-helper/.clean :
	rm("utils/build-utils/modules-test-helper/target");
