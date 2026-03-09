libs/osgi-libs/saxon/VERSION := 10.5-p1-SNAPSHOT

$(TARGET_DIR)/state/libs/osgi-libs/saxon/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : libs/osgi-libs/saxon/.test
libs/osgi-libs/saxon/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/saxon/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/saxon-he/10.5-p1-SNAPSHOT/saxon-he-10.5-p1-SNAPSHOT.pom : libs/osgi-libs/saxon/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/saxon-he/10.5-p1-SNAPSHOT/saxon-he-10.5-p1-SNAPSHOT% : libs/osgi-libs/saxon/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : libs/osgi-libs/saxon/.install.pom
libs/osgi-libs/saxon/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("libs/osgi-libs/saxon");

libs/osgi-libs/saxon/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : libs/osgi-libs/saxon/.install.jar
libs/osgi-libs/saxon/.install.jar : %/.install.jar : %/.install

.SECONDARY : libs/osgi-libs/saxon/.install
libs/osgi-libs/saxon/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/saxon/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : libs/osgi-libs/saxon/.install-doc
libs/osgi-libs/saxon/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/saxon/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : libs/osgi-libs/saxon/.compile-dependencies libs/osgi-libs/saxon/.test-dependencies
libs/osgi-libs/saxon/.compile-dependencies :
libs/osgi-libs/saxon/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/saxon-he/10.5-p1/saxon-he-10.5-p1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/saxon-he/10.5-p1/saxon-he-10.5-p1-% : libs/osgi-libs/saxon/.release
	+//

.SECONDARY : libs/osgi-libs/saxon/.release
libs/osgi-libs/saxon/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/saxon/.release :

clean : libs/osgi-libs/saxon/.clean
.PHONY : libs/osgi-libs/saxon/.clean
libs/osgi-libs/saxon/.clean :
	rm("libs/osgi-libs/saxon/target");
