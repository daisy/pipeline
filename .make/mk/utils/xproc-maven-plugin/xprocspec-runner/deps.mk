utils/xproc-maven-plugin/xprocspec-runner/VERSION := 1.2.9-SNAPSHOT

$(TARGET_DIR)/state/utils/xproc-maven-plugin/xprocspec-runner/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : utils/xproc-maven-plugin/xprocspec-runner/.test
utils/xproc-maven-plugin/xprocspec-runner/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

utils/xproc-maven-plugin/xprocspec-runner/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xprocspec-runner/1.2.9-SNAPSHOT/xprocspec-runner-1.2.9-SNAPSHOT.pom : utils/xproc-maven-plugin/xprocspec-runner/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xprocspec-runner/1.2.9-SNAPSHOT/xprocspec-runner-1.2.9-SNAPSHOT% : utils/xproc-maven-plugin/xprocspec-runner/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : utils/xproc-maven-plugin/xprocspec-runner/.install.pom
utils/xproc-maven-plugin/xprocspec-runner/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("utils/xproc-maven-plugin/xprocspec-runner");

utils/xproc-maven-plugin/xprocspec-runner/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/xproc-maven-plugin/xprocspec-runner/.install.jar
utils/xproc-maven-plugin/xprocspec-runner/.install.jar : %/.install.jar : %/.install

.SECONDARY : utils/xproc-maven-plugin/xprocspec-runner/.install
utils/xproc-maven-plugin/xprocspec-runner/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

utils/xproc-maven-plugin/xprocspec-runner/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/xproc-maven-plugin/xprocspec-runner/.install-doc
utils/xproc-maven-plugin/xprocspec-runner/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

utils/xproc-maven-plugin/xprocspec-runner/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : utils/xproc-maven-plugin/xprocspec-runner/.compile-dependencies utils/xproc-maven-plugin/xprocspec-runner/.test-dependencies
utils/xproc-maven-plugin/xprocspec-runner/.compile-dependencies :
utils/xproc-maven-plugin/xprocspec-runner/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xprocspec-runner/1.2.9/xprocspec-runner-1.2.9.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xprocspec-runner/1.2.9/xprocspec-runner-1.2.9-% : utils/xproc-maven-plugin/xprocspec-runner/.release
	+//

.SECONDARY : utils/xproc-maven-plugin/xprocspec-runner/.release
utils/xproc-maven-plugin/xprocspec-runner/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

utils/xproc-maven-plugin/xprocspec-runner/.release :

clean : utils/xproc-maven-plugin/xprocspec-runner/.clean
.PHONY : utils/xproc-maven-plugin/xprocspec-runner/.clean
utils/xproc-maven-plugin/xprocspec-runner/.clean :
	rm("utils/xproc-maven-plugin/xprocspec-runner/target");
