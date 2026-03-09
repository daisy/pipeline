clientlib/java/clientlib-java-httpclient/VERSION := 2.1.3-SNAPSHOT

$(TARGET_DIR)/state/clientlib/java/clientlib-java-httpclient/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : clientlib/java/clientlib-java-httpclient/.test
clientlib/java/clientlib-java-httpclient/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

clientlib/java/clientlib-java-httpclient/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/clientlib-java-httpclient/2.1.3-SNAPSHOT/clientlib-java-httpclient-2.1.3-SNAPSHOT.pom : clientlib/java/clientlib-java-httpclient/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/clientlib-java-httpclient/2.1.3-SNAPSHOT/clientlib-java-httpclient-2.1.3-SNAPSHOT% : clientlib/java/clientlib-java-httpclient/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : clientlib/java/clientlib-java-httpclient/.install.pom
clientlib/java/clientlib-java-httpclient/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("clientlib/java/clientlib-java-httpclient");

clientlib/java/clientlib-java-httpclient/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : clientlib/java/clientlib-java-httpclient/.install.jar
clientlib/java/clientlib-java-httpclient/.install.jar : %/.install.jar : %/.install

.SECONDARY : clientlib/java/clientlib-java-httpclient/.install
clientlib/java/clientlib-java-httpclient/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

clientlib/java/clientlib-java-httpclient/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : clientlib/java/clientlib-java-httpclient/.install-javadoc.jar
clientlib/java/clientlib-java-httpclient/.install-javadoc.jar : %/.install-javadoc.jar : %/.install-doc

.SECONDARY : clientlib/java/clientlib-java-httpclient/.install-doc
clientlib/java/clientlib-java-httpclient/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

clientlib/java/clientlib-java-httpclient/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : clientlib/java/clientlib-java-httpclient/.compile-dependencies clientlib/java/clientlib-java-httpclient/.test-dependencies
clientlib/java/clientlib-java-httpclient/.compile-dependencies :
clientlib/java/clientlib-java-httpclient/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/clientlib-java-httpclient/2.1.3/clientlib-java-httpclient-2.1.3.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/clientlib-java-httpclient/2.1.3/clientlib-java-httpclient-2.1.3-% : clientlib/java/clientlib-java-httpclient/.release
	+//

.SECONDARY : clientlib/java/clientlib-java-httpclient/.release
clientlib/java/clientlib-java-httpclient/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

clientlib/java/clientlib-java-httpclient/.release :

clean : clientlib/java/clientlib-java-httpclient/.clean
.PHONY : clientlib/java/clientlib-java-httpclient/.clean
clientlib/java/clientlib-java-httpclient/.clean :
	rm("clientlib/java/clientlib-java-httpclient/target");
