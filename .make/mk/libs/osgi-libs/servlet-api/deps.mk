libs/osgi-libs/servlet-api/VERSION := 3.1.0

$(TARGET_DIR)/state/libs/osgi-libs/servlet-api/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : libs/osgi-libs/servlet-api/.test
libs/osgi-libs/servlet-api/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/servlet-api/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/servlet-api/3.1.0/servlet-api-3.1.0.pom : libs/osgi-libs/servlet-api/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/servlet-api/3.1.0/servlet-api-3.1.0% : libs/osgi-libs/servlet-api/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : libs/osgi-libs/servlet-api/.install.pom
libs/osgi-libs/servlet-api/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("libs/osgi-libs/servlet-api");

libs/osgi-libs/servlet-api/.install.pom : %/.install.pom : | %/.compile-dependencies %/.test-dependencies

.SECONDARY : libs/osgi-libs/servlet-api/.install.jar
libs/osgi-libs/servlet-api/.install.jar : %/.install.jar : %/.install

.SECONDARY : libs/osgi-libs/servlet-api/.install
libs/osgi-libs/servlet-api/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/servlet-api/.install : %/.install : | %/.compile-dependencies %/.test-dependencies

.SECONDARY : libs/osgi-libs/servlet-api/.install-doc
libs/osgi-libs/servlet-api/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/servlet-api/.install-doc : %/.install-doc : | %/.compile-dependencies %/.test-dependencies

.SECONDARY : libs/osgi-libs/servlet-api/.compile-dependencies libs/osgi-libs/servlet-api/.test-dependencies
libs/osgi-libs/servlet-api/.compile-dependencies :
libs/osgi-libs/servlet-api/.test-dependencies :

.SECONDARY : libs/osgi-libs/servlet-api/.release

clean : libs/osgi-libs/servlet-api/.clean
.PHONY : libs/osgi-libs/servlet-api/.clean
libs/osgi-libs/servlet-api/.clean :
	rm("libs/osgi-libs/servlet-api/target");
