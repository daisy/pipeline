libs/liblouis-java/VERSION := 5.1.0

$(TARGET_DIR)/state/libs/liblouis-java/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : libs/liblouis-java/.test
libs/liblouis-java/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

libs/liblouis-java/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/liblouis/liblouis-java/5.1.0/liblouis-java-5.1.0.pom : libs/liblouis-java/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/liblouis/liblouis-java/5.1.0/liblouis-java-5.1.0% : libs/liblouis-java/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : libs/liblouis-java/.install.pom
libs/liblouis-java/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("libs/liblouis-java");

libs/liblouis-java/.install.pom : %/.install.pom : | %/.compile-dependencies %/.test-dependencies

.SECONDARY : libs/liblouis-java/.install.jar
libs/liblouis-java/.install.jar : %/.install.jar : %/.install

.SECONDARY : libs/liblouis-java/.install
libs/liblouis-java/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

libs/liblouis-java/.install : %/.install : | %/.compile-dependencies %/.test-dependencies

.SECONDARY : libs/liblouis-java/.install-doc
libs/liblouis-java/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

libs/liblouis-java/.install-doc : %/.install-doc : | %/.compile-dependencies %/.test-dependencies

.SECONDARY : libs/liblouis-java/.compile-dependencies libs/liblouis-java/.test-dependencies
libs/liblouis-java/.compile-dependencies :
libs/liblouis-java/.test-dependencies :

.SECONDARY : libs/liblouis-java/.release

clean : libs/liblouis-java/.clean
.PHONY : libs/liblouis-java/.clean
libs/liblouis-java/.clean :
	rm("libs/liblouis-java/target");
