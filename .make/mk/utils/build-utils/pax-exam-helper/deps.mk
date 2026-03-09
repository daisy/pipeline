utils/build-utils/pax-exam-helper/VERSION := 2.5.2-SNAPSHOT

$(TARGET_DIR)/state/utils/build-utils/pax-exam-helper/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : utils/build-utils/pax-exam-helper/.test
utils/build-utils/pax-exam-helper/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

utils/build-utils/pax-exam-helper/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/pax-exam-helper/2.5.2-SNAPSHOT/pax-exam-helper-2.5.2-SNAPSHOT.pom : utils/build-utils/pax-exam-helper/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/pax-exam-helper/2.5.2-SNAPSHOT/pax-exam-helper-2.5.2-SNAPSHOT% : utils/build-utils/pax-exam-helper/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : utils/build-utils/pax-exam-helper/.install.pom
utils/build-utils/pax-exam-helper/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("utils/build-utils/pax-exam-helper");

utils/build-utils/pax-exam-helper/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/build-utils/pax-exam-helper/.install.jar
utils/build-utils/pax-exam-helper/.install.jar : %/.install.jar : %/.install

.SECONDARY : utils/build-utils/pax-exam-helper/.install
utils/build-utils/pax-exam-helper/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

utils/build-utils/pax-exam-helper/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/build-utils/pax-exam-helper/.install-doc
utils/build-utils/pax-exam-helper/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

utils/build-utils/pax-exam-helper/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : utils/build-utils/pax-exam-helper/.compile-dependencies utils/build-utils/pax-exam-helper/.test-dependencies
utils/build-utils/pax-exam-helper/.compile-dependencies :
utils/build-utils/pax-exam-helper/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/pax-exam-helper/2.5.2/pax-exam-helper-2.5.2.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/pax-exam-helper/2.5.2/pax-exam-helper-2.5.2-% : utils/build-utils/pax-exam-helper/.release
	+//

.SECONDARY : utils/build-utils/pax-exam-helper/.release
utils/build-utils/pax-exam-helper/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

utils/build-utils/pax-exam-helper/.release :

clean : utils/build-utils/pax-exam-helper/.clean
.PHONY : utils/build-utils/pax-exam-helper/.clean
utils/build-utils/pax-exam-helper/.clean :
	rm("utils/build-utils/pax-exam-helper/target");
