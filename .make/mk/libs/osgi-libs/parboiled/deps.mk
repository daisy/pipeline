libs/osgi-libs/parboiled/VERSION := 1.0.1-SNAPSHOT

$(TARGET_DIR)/state/libs/osgi-libs/parboiled/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : libs/osgi-libs/parboiled/.test
libs/osgi-libs/parboiled/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/parboiled/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/parboiled/1.0.1-SNAPSHOT/parboiled-1.0.1-SNAPSHOT.pom : libs/osgi-libs/parboiled/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/parboiled/1.0.1-SNAPSHOT/parboiled-1.0.1-SNAPSHOT% : libs/osgi-libs/parboiled/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : libs/osgi-libs/parboiled/.install.pom
libs/osgi-libs/parboiled/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("libs/osgi-libs/parboiled");

libs/osgi-libs/parboiled/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : libs/osgi-libs/parboiled/.install.jar
libs/osgi-libs/parboiled/.install.jar : %/.install.jar : %/.install

.SECONDARY : libs/osgi-libs/parboiled/.install
libs/osgi-libs/parboiled/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/parboiled/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : libs/osgi-libs/parboiled/.install-doc
libs/osgi-libs/parboiled/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/parboiled/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : libs/osgi-libs/parboiled/.compile-dependencies libs/osgi-libs/parboiled/.test-dependencies
libs/osgi-libs/parboiled/.compile-dependencies :
libs/osgi-libs/parboiled/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/parboiled/1.0.1/parboiled-1.0.1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/parboiled/1.0.1/parboiled-1.0.1-% : libs/osgi-libs/parboiled/.release
	+//

.SECONDARY : libs/osgi-libs/parboiled/.release
libs/osgi-libs/parboiled/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/parboiled/.release :

clean : libs/osgi-libs/parboiled/.clean
.PHONY : libs/osgi-libs/parboiled/.clean
libs/osgi-libs/parboiled/.clean :
	rm("libs/osgi-libs/parboiled/target");
