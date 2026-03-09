libs/osgi-libs/pegdown/VERSION := 1.0.1-SNAPSHOT

$(TARGET_DIR)/state/libs/osgi-libs/pegdown/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : libs/osgi-libs/pegdown/.test
libs/osgi-libs/pegdown/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/pegdown/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/pegdown/1.0.1-SNAPSHOT/pegdown-1.0.1-SNAPSHOT.pom : libs/osgi-libs/pegdown/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/pegdown/1.0.1-SNAPSHOT/pegdown-1.0.1-SNAPSHOT% : libs/osgi-libs/pegdown/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : libs/osgi-libs/pegdown/.install.pom
libs/osgi-libs/pegdown/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("libs/osgi-libs/pegdown");

libs/osgi-libs/pegdown/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : libs/osgi-libs/pegdown/.install.jar
libs/osgi-libs/pegdown/.install.jar : %/.install.jar : %/.install

.SECONDARY : libs/osgi-libs/pegdown/.install
libs/osgi-libs/pegdown/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/pegdown/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : libs/osgi-libs/pegdown/.install-doc
libs/osgi-libs/pegdown/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/pegdown/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : libs/osgi-libs/pegdown/.compile-dependencies libs/osgi-libs/pegdown/.test-dependencies
libs/osgi-libs/pegdown/.compile-dependencies :
libs/osgi-libs/pegdown/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/pegdown/1.0.1/pegdown-1.0.1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/pegdown/1.0.1/pegdown-1.0.1-% : libs/osgi-libs/pegdown/.release
	+//

.SECONDARY : libs/osgi-libs/pegdown/.release
libs/osgi-libs/pegdown/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/pegdown/.release :

clean : libs/osgi-libs/pegdown/.clean
.PHONY : libs/osgi-libs/pegdown/.clean
libs/osgi-libs/pegdown/.clean :
	rm("libs/osgi-libs/pegdown/target");
