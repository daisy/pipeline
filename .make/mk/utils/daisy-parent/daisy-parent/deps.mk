utils/daisy-parent/daisy-parent/VERSION := 6-SNAPSHOT

$(TARGET_DIR)/state/utils/daisy-parent/daisy-parent/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : utils/daisy-parent/daisy-parent/.test
utils/daisy-parent/daisy-parent/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

utils/daisy-parent/daisy-parent/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/daisy/6-SNAPSHOT/daisy-6-SNAPSHOT.pom : utils/daisy-parent/daisy-parent/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/daisy/6-SNAPSHOT/daisy-6-SNAPSHOT% : utils/daisy-parent/daisy-parent/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : utils/daisy-parent/daisy-parent/.install.pom
utils/daisy-parent/daisy-parent/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("utils/daisy-parent/daisy-parent");

utils/daisy-parent/daisy-parent/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/daisy-parent/daisy-parent/.install-doc
utils/daisy-parent/daisy-parent/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

utils/daisy-parent/daisy-parent/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : utils/daisy-parent/daisy-parent/.compile-dependencies utils/daisy-parent/daisy-parent/.test-dependencies
utils/daisy-parent/daisy-parent/.compile-dependencies :
utils/daisy-parent/daisy-parent/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/daisy/6/daisy-6.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/daisy/6/daisy-6-% : utils/daisy-parent/daisy-parent/.release
	+//

.SECONDARY : utils/daisy-parent/daisy-parent/.release
utils/daisy-parent/daisy-parent/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

utils/daisy-parent/daisy-parent/.release :

clean : utils/daisy-parent/daisy-parent/.clean
.PHONY : utils/daisy-parent/daisy-parent/.clean
utils/daisy-parent/daisy-parent/.clean :
	rm("utils/daisy-parent/daisy-parent/target");
