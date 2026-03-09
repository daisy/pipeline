utils/xspec-maven-plugin/xspec-maven-plugin/VERSION := 1.0.2-SNAPSHOT

$(TARGET_DIR)/state/utils/xspec-maven-plugin/xspec-maven-plugin/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : utils/xspec-maven-plugin/xspec-maven-plugin/.test
utils/xspec-maven-plugin/xspec-maven-plugin/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

utils/xspec-maven-plugin/xspec-maven-plugin/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xspec-maven-plugin/1.0.2-SNAPSHOT/xspec-maven-plugin-1.0.2-SNAPSHOT.pom : utils/xspec-maven-plugin/xspec-maven-plugin/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xspec-maven-plugin/1.0.2-SNAPSHOT/xspec-maven-plugin-1.0.2-SNAPSHOT% : utils/xspec-maven-plugin/xspec-maven-plugin/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : utils/xspec-maven-plugin/xspec-maven-plugin/.install.pom
utils/xspec-maven-plugin/xspec-maven-plugin/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("utils/xspec-maven-plugin/xspec-maven-plugin");

utils/xspec-maven-plugin/xspec-maven-plugin/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/xspec-maven-plugin/xspec-maven-plugin/.install.jar
utils/xspec-maven-plugin/xspec-maven-plugin/.install.jar : %/.install.jar : %/.install

.SECONDARY : utils/xspec-maven-plugin/xspec-maven-plugin/.install
utils/xspec-maven-plugin/xspec-maven-plugin/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

utils/xspec-maven-plugin/xspec-maven-plugin/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/xspec-maven-plugin/xspec-maven-plugin/.install-doc
utils/xspec-maven-plugin/xspec-maven-plugin/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

utils/xspec-maven-plugin/xspec-maven-plugin/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : utils/xspec-maven-plugin/xspec-maven-plugin/.compile-dependencies utils/xspec-maven-plugin/xspec-maven-plugin/.test-dependencies
utils/xspec-maven-plugin/xspec-maven-plugin/.compile-dependencies :
utils/xspec-maven-plugin/xspec-maven-plugin/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xspec-maven-plugin/1.0.2/xspec-maven-plugin-1.0.2.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xspec-maven-plugin/1.0.2/xspec-maven-plugin-1.0.2-% : utils/xspec-maven-plugin/xspec-maven-plugin/.release
	+//

.SECONDARY : utils/xspec-maven-plugin/xspec-maven-plugin/.release
utils/xspec-maven-plugin/xspec-maven-plugin/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

utils/xspec-maven-plugin/xspec-maven-plugin/.release :

clean : utils/xspec-maven-plugin/xspec-maven-plugin/.clean
.PHONY : utils/xspec-maven-plugin/xspec-maven-plugin/.clean
utils/xspec-maven-plugin/xspec-maven-plugin/.clean :
	rm("utils/xspec-maven-plugin/xspec-maven-plugin/target");
