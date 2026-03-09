utils/xproc-maven-plugin/xproc-engine-api/VERSION := 1.3.1-SNAPSHOT

$(TARGET_DIR)/state/utils/xproc-maven-plugin/xproc-engine-api/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : utils/xproc-maven-plugin/xproc-engine-api/.test
utils/xproc-maven-plugin/xproc-engine-api/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

utils/xproc-maven-plugin/xproc-engine-api/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-api/1.3.1-SNAPSHOT/xproc-engine-api-1.3.1-SNAPSHOT.pom : utils/xproc-maven-plugin/xproc-engine-api/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-api/1.3.1-SNAPSHOT/xproc-engine-api-1.3.1-SNAPSHOT% : utils/xproc-maven-plugin/xproc-engine-api/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : utils/xproc-maven-plugin/xproc-engine-api/.install.pom
utils/xproc-maven-plugin/xproc-engine-api/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("utils/xproc-maven-plugin/xproc-engine-api");

utils/xproc-maven-plugin/xproc-engine-api/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/xproc-maven-plugin/xproc-engine-api/.install.jar
utils/xproc-maven-plugin/xproc-engine-api/.install.jar : %/.install.jar : %/.install

.SECONDARY : utils/xproc-maven-plugin/xproc-engine-api/.install
utils/xproc-maven-plugin/xproc-engine-api/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

utils/xproc-maven-plugin/xproc-engine-api/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/xproc-maven-plugin/xproc-engine-api/.install-doc
utils/xproc-maven-plugin/xproc-engine-api/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

utils/xproc-maven-plugin/xproc-engine-api/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : utils/xproc-maven-plugin/xproc-engine-api/.compile-dependencies utils/xproc-maven-plugin/xproc-engine-api/.test-dependencies
utils/xproc-maven-plugin/xproc-engine-api/.compile-dependencies :
utils/xproc-maven-plugin/xproc-engine-api/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-api/1.3.1/xproc-engine-api-1.3.1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-api/1.3.1/xproc-engine-api-1.3.1-% : utils/xproc-maven-plugin/xproc-engine-api/.release
	+//

.SECONDARY : utils/xproc-maven-plugin/xproc-engine-api/.release
utils/xproc-maven-plugin/xproc-engine-api/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

utils/xproc-maven-plugin/xproc-engine-api/.release :

clean : utils/xproc-maven-plugin/xproc-engine-api/.clean
.PHONY : utils/xproc-maven-plugin/xproc-engine-api/.clean
utils/xproc-maven-plugin/xproc-engine-api/.clean :
	rm("utils/xproc-maven-plugin/xproc-engine-api/target");
