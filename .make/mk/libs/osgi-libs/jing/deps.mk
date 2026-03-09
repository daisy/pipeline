libs/osgi-libs/jing/VERSION := 20151127.0.2-SNAPSHOT

$(TARGET_DIR)/state/libs/osgi-libs/jing/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : libs/osgi-libs/jing/.test
libs/osgi-libs/jing/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/jing/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/jing/20151127.0.2-SNAPSHOT/jing-20151127.0.2-SNAPSHOT.pom : libs/osgi-libs/jing/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/jing/20151127.0.2-SNAPSHOT/jing-20151127.0.2-SNAPSHOT% : libs/osgi-libs/jing/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : libs/osgi-libs/jing/.install.pom
libs/osgi-libs/jing/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("libs/osgi-libs/jing");

libs/osgi-libs/jing/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : libs/osgi-libs/jing/.install.jar
libs/osgi-libs/jing/.install.jar : %/.install.jar : %/.install

.SECONDARY : libs/osgi-libs/jing/.install
libs/osgi-libs/jing/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/jing/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : libs/osgi-libs/jing/.install-doc
libs/osgi-libs/jing/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/jing/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : libs/osgi-libs/jing/.compile-dependencies libs/osgi-libs/jing/.test-dependencies
libs/osgi-libs/jing/.compile-dependencies :
libs/osgi-libs/jing/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/jing/20151127.0.2/jing-20151127.0.2.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/jing/20151127.0.2/jing-20151127.0.2-% : libs/osgi-libs/jing/.release
	+//

.SECONDARY : libs/osgi-libs/jing/.release
libs/osgi-libs/jing/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/jing/.release :

clean : libs/osgi-libs/jing/.clean
.PHONY : libs/osgi-libs/jing/.clean
libs/osgi-libs/jing/.clean :
	rm("libs/osgi-libs/jing/target");
