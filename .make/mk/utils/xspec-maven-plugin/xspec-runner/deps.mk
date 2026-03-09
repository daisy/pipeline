utils/xspec-maven-plugin/xspec-runner/VERSION := 1.0.6-SNAPSHOT

$(TARGET_DIR)/state/utils/xspec-maven-plugin/xspec-runner/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : utils/xspec-maven-plugin/xspec-runner/.test
utils/xspec-maven-plugin/xspec-runner/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

utils/xspec-maven-plugin/xspec-runner/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xspec-runner/1.0.6-SNAPSHOT/xspec-runner-1.0.6-SNAPSHOT.pom : utils/xspec-maven-plugin/xspec-runner/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xspec-runner/1.0.6-SNAPSHOT/xspec-runner-1.0.6-SNAPSHOT% : utils/xspec-maven-plugin/xspec-runner/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : utils/xspec-maven-plugin/xspec-runner/.install.pom
utils/xspec-maven-plugin/xspec-runner/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("utils/xspec-maven-plugin/xspec-runner");

utils/xspec-maven-plugin/xspec-runner/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/xspec-maven-plugin/xspec-runner/.install.jar
utils/xspec-maven-plugin/xspec-runner/.install.jar : %/.install.jar : %/.install

.SECONDARY : utils/xspec-maven-plugin/xspec-runner/.install
utils/xspec-maven-plugin/xspec-runner/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

utils/xspec-maven-plugin/xspec-runner/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/xspec-maven-plugin/xspec-runner/.install-doc
utils/xspec-maven-plugin/xspec-runner/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

utils/xspec-maven-plugin/xspec-runner/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : utils/xspec-maven-plugin/xspec-runner/.compile-dependencies utils/xspec-maven-plugin/xspec-runner/.test-dependencies
utils/xspec-maven-plugin/xspec-runner/.compile-dependencies :
utils/xspec-maven-plugin/xspec-runner/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xspec-runner/1.0.6/xspec-runner-1.0.6.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xspec-runner/1.0.6/xspec-runner-1.0.6-% : utils/xspec-maven-plugin/xspec-runner/.release
	+//

.SECONDARY : utils/xspec-maven-plugin/xspec-runner/.release
utils/xspec-maven-plugin/xspec-runner/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

utils/xspec-maven-plugin/xspec-runner/.release :

clean : utils/xspec-maven-plugin/xspec-runner/.clean
.PHONY : utils/xspec-maven-plugin/xspec-runner/.clean
utils/xspec-maven-plugin/xspec-runner/.clean :
	rm("utils/xspec-maven-plugin/xspec-runner/target");
