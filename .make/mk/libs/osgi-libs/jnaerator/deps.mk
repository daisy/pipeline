libs/osgi-libs/jnaerator/VERSION := 0.11-p2-SNAPSHOT

$(TARGET_DIR)/state/libs/osgi-libs/jnaerator/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : libs/osgi-libs/jnaerator/.test
libs/osgi-libs/jnaerator/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/jnaerator/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/jnaerator/0.11-p2-SNAPSHOT/jnaerator-0.11-p2-SNAPSHOT.pom : libs/osgi-libs/jnaerator/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/jnaerator/0.11-p2-SNAPSHOT/jnaerator-0.11-p2-SNAPSHOT% : libs/osgi-libs/jnaerator/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : libs/osgi-libs/jnaerator/.install.pom
libs/osgi-libs/jnaerator/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("libs/osgi-libs/jnaerator");

libs/osgi-libs/jnaerator/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : libs/osgi-libs/jnaerator/.install.jar
libs/osgi-libs/jnaerator/.install.jar : %/.install.jar : %/.install

.SECONDARY : libs/osgi-libs/jnaerator/.install
libs/osgi-libs/jnaerator/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/jnaerator/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : libs/osgi-libs/jnaerator/.install-doc
libs/osgi-libs/jnaerator/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/jnaerator/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : libs/osgi-libs/jnaerator/.compile-dependencies libs/osgi-libs/jnaerator/.test-dependencies
libs/osgi-libs/jnaerator/.compile-dependencies :
libs/osgi-libs/jnaerator/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/jnaerator/0.11-p2/jnaerator-0.11-p2.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/jnaerator/0.11-p2/jnaerator-0.11-p2-% : libs/osgi-libs/jnaerator/.release
	+//

.SECONDARY : libs/osgi-libs/jnaerator/.release
libs/osgi-libs/jnaerator/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

libs/osgi-libs/jnaerator/.release :

clean : libs/osgi-libs/jnaerator/.clean
.PHONY : libs/osgi-libs/jnaerator/.clean
libs/osgi-libs/jnaerator/.clean :
	rm("libs/osgi-libs/jnaerator/target");
