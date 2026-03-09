utils/build-utils/modules-build-helper/VERSION := 3.0.1

$(TARGET_DIR)/state/utils/build-utils/modules-build-helper/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : utils/build-utils/modules-build-helper/.test
utils/build-utils/modules-build-helper/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

utils/build-utils/modules-build-helper/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-build-helper/3.0.1/modules-build-helper-3.0.1.pom : utils/build-utils/modules-build-helper/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-build-helper/3.0.1/modules-build-helper-3.0.1% : utils/build-utils/modules-build-helper/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : utils/build-utils/modules-build-helper/.install.pom
utils/build-utils/modules-build-helper/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("utils/build-utils/modules-build-helper");

utils/build-utils/modules-build-helper/.install.pom : %/.install.pom : | %/.compile-dependencies %/.test-dependencies

.SECONDARY : utils/build-utils/modules-build-helper/.install.jar
utils/build-utils/modules-build-helper/.install.jar : %/.install.jar : %/.install

.SECONDARY : utils/build-utils/modules-build-helper/.install
utils/build-utils/modules-build-helper/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

utils/build-utils/modules-build-helper/.install : %/.install : | %/.compile-dependencies %/.test-dependencies

.SECONDARY : utils/build-utils/modules-build-helper/.install-doc
utils/build-utils/modules-build-helper/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

utils/build-utils/modules-build-helper/.install-doc : %/.install-doc : | %/.compile-dependencies %/.test-dependencies

.SECONDARY : utils/build-utils/modules-build-helper/.compile-dependencies utils/build-utils/modules-build-helper/.test-dependencies
utils/build-utils/modules-build-helper/.compile-dependencies :
utils/build-utils/modules-build-helper/.test-dependencies :

.SECONDARY : utils/build-utils/modules-build-helper/.release

clean : utils/build-utils/modules-build-helper/.clean
.PHONY : utils/build-utils/modules-build-helper/.clean
utils/build-utils/modules-build-helper/.clean :
	rm("utils/build-utils/modules-build-helper/target");
