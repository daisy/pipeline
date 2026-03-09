clientlib/java/clientlib-java/VERSION := 5.0.1

$(TARGET_DIR)/state/clientlib/java/clientlib-java/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : clientlib/java/clientlib-java/.test
clientlib/java/clientlib-java/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

clientlib/java/clientlib-java/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/clientlib-java/5.0.1/clientlib-java-5.0.1.pom : clientlib/java/clientlib-java/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/clientlib-java/5.0.1/clientlib-java-5.0.1% : clientlib/java/clientlib-java/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : clientlib/java/clientlib-java/.install.pom
clientlib/java/clientlib-java/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("clientlib/java/clientlib-java");

clientlib/java/clientlib-java/.install.pom : %/.install.pom : | %/.compile-dependencies %/.test-dependencies

.SECONDARY : clientlib/java/clientlib-java/.install.jar
clientlib/java/clientlib-java/.install.jar : %/.install.jar : %/.install

.SECONDARY : clientlib/java/clientlib-java/.install
clientlib/java/clientlib-java/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

clientlib/java/clientlib-java/.install : %/.install : | %/.compile-dependencies %/.test-dependencies

.SECONDARY : clientlib/java/clientlib-java/.install-javadoc.jar
clientlib/java/clientlib-java/.install-javadoc.jar : %/.install-javadoc.jar : %/.install-doc

.SECONDARY : clientlib/java/clientlib-java/.install-doc
clientlib/java/clientlib-java/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

clientlib/java/clientlib-java/.install-doc : %/.install-doc : | %/.compile-dependencies %/.test-dependencies

.SECONDARY : clientlib/java/clientlib-java/.compile-dependencies clientlib/java/clientlib-java/.test-dependencies
clientlib/java/clientlib-java/.compile-dependencies :
clientlib/java/clientlib-java/.test-dependencies :

.SECONDARY : clientlib/java/clientlib-java/.release

clean : clientlib/java/clientlib-java/.clean
.PHONY : clientlib/java/clientlib-java/.clean
clientlib/java/clientlib-java/.clean :
	rm("clientlib/java/clientlib-java/target");
