utils/xproc-maven-plugin/xproc-engine-calabash/VERSION := 1.2.1-SNAPSHOT

$(TARGET_DIR)/state/utils/xproc-maven-plugin/xproc-engine-calabash/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : utils/xproc-maven-plugin/xproc-engine-calabash/.test
utils/xproc-maven-plugin/xproc-engine-calabash/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

utils/xproc-maven-plugin/xproc-engine-calabash/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-calabash/1.2.1-SNAPSHOT/xproc-engine-calabash-1.2.1-SNAPSHOT.pom : utils/xproc-maven-plugin/xproc-engine-calabash/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-calabash/1.2.1-SNAPSHOT/xproc-engine-calabash-1.2.1-SNAPSHOT% : utils/xproc-maven-plugin/xproc-engine-calabash/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : utils/xproc-maven-plugin/xproc-engine-calabash/.install.pom
utils/xproc-maven-plugin/xproc-engine-calabash/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("utils/xproc-maven-plugin/xproc-engine-calabash");

utils/xproc-maven-plugin/xproc-engine-calabash/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/xproc-maven-plugin/xproc-engine-calabash/.install.jar
utils/xproc-maven-plugin/xproc-engine-calabash/.install.jar : %/.install.jar : %/.install

.SECONDARY : utils/xproc-maven-plugin/xproc-engine-calabash/.install
utils/xproc-maven-plugin/xproc-engine-calabash/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

utils/xproc-maven-plugin/xproc-engine-calabash/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/xproc-maven-plugin/xproc-engine-calabash/.install-doc
utils/xproc-maven-plugin/xproc-engine-calabash/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

utils/xproc-maven-plugin/xproc-engine-calabash/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : utils/xproc-maven-plugin/xproc-engine-calabash/.compile-dependencies utils/xproc-maven-plugin/xproc-engine-calabash/.test-dependencies
utils/xproc-maven-plugin/xproc-engine-calabash/.compile-dependencies :
utils/xproc-maven-plugin/xproc-engine-calabash/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-calabash/1.2.1/xproc-engine-calabash-1.2.1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-calabash/1.2.1/xproc-engine-calabash-1.2.1-% : utils/xproc-maven-plugin/xproc-engine-calabash/.release
	+//

.SECONDARY : utils/xproc-maven-plugin/xproc-engine-calabash/.release
utils/xproc-maven-plugin/xproc-engine-calabash/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

utils/xproc-maven-plugin/xproc-engine-calabash/.release :

clean : utils/xproc-maven-plugin/xproc-engine-calabash/.clean
.PHONY : utils/xproc-maven-plugin/xproc-engine-calabash/.clean
utils/xproc-maven-plugin/xproc-engine-calabash/.clean :
	rm("utils/xproc-maven-plugin/xproc-engine-calabash/target");
