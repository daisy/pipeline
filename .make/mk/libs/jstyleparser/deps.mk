libs/jstyleparser/VERSION := 1.20-p26

$(TARGET_DIR)/state/libs/jstyleparser/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : libs/jstyleparser/.test
libs/jstyleparser/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

libs/jstyleparser/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/jstyleparser/1.20-p26/jstyleparser-1.20-p26.pom : libs/jstyleparser/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/jstyleparser/1.20-p26/jstyleparser-1.20-p26% : libs/jstyleparser/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : libs/jstyleparser/.install.pom
libs/jstyleparser/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("libs/jstyleparser");

libs/jstyleparser/.install.pom : %/.install.pom : | %/.compile-dependencies %/.test-dependencies

.SECONDARY : libs/jstyleparser/.install.jar
libs/jstyleparser/.install.jar : %/.install.jar : %/.install

.SECONDARY : libs/jstyleparser/.install
libs/jstyleparser/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

libs/jstyleparser/.install : %/.install : | %/.compile-dependencies %/.test-dependencies

.SECONDARY : libs/jstyleparser/.install-doc
libs/jstyleparser/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

libs/jstyleparser/.install-doc : %/.install-doc : | %/.compile-dependencies %/.test-dependencies

.SECONDARY : libs/jstyleparser/.compile-dependencies libs/jstyleparser/.test-dependencies
libs/jstyleparser/.compile-dependencies :
libs/jstyleparser/.test-dependencies :

.SECONDARY : libs/jstyleparser/.release

clean : libs/jstyleparser/.clean
.PHONY : libs/jstyleparser/.clean
libs/jstyleparser/.clean :
	rm("libs/jstyleparser/target");
