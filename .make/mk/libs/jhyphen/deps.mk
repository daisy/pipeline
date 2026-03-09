libs/jhyphen/VERSION := 1.0.5-SNAPSHOT

$(TARGET_DIR)/state/libs/jhyphen/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : libs/jhyphen/.test
libs/jhyphen/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

libs/jhyphen/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/bindings/jhyphen/1.0.5-SNAPSHOT/jhyphen-1.0.5-SNAPSHOT.pom : libs/jhyphen/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/bindings/jhyphen/1.0.5-SNAPSHOT/jhyphen-1.0.5-SNAPSHOT% : libs/jhyphen/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : libs/jhyphen/.install.pom
libs/jhyphen/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("libs/jhyphen");

libs/jhyphen/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : libs/jhyphen/.install.jar
libs/jhyphen/.install.jar : %/.install.jar : %/.install

.SECONDARY : libs/jhyphen/.install
libs/jhyphen/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

libs/jhyphen/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : libs/jhyphen/.install-doc
libs/jhyphen/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

libs/jhyphen/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : libs/jhyphen/.compile-dependencies libs/jhyphen/.test-dependencies
libs/jhyphen/.compile-dependencies :
libs/jhyphen/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/bindings/jhyphen/1.0.5/jhyphen-1.0.5.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/bindings/jhyphen/1.0.5/jhyphen-1.0.5-% : libs/jhyphen/.release
	+//

.SECONDARY : libs/jhyphen/.release
libs/jhyphen/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

libs/jhyphen/.release :

clean : libs/jhyphen/.clean
.PHONY : libs/jhyphen/.clean
libs/jhyphen/.clean :
	rm("libs/jhyphen/target");
