utils/build-utils/ds-to-spi/ds-to-spi-annotations/VERSION := 1.0.1-SNAPSHOT

$(TARGET_DIR)/state/utils/build-utils/ds-to-spi/ds-to-spi-annotations/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-annotations/.test
utils/build-utils/ds-to-spi/ds-to-spi-annotations/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

utils/build-utils/ds-to-spi/ds-to-spi-annotations/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/ds-to-spi-annotations/1.0.1-SNAPSHOT/ds-to-spi-annotations-1.0.1-SNAPSHOT.pom : utils/build-utils/ds-to-spi/ds-to-spi-annotations/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/ds-to-spi-annotations/1.0.1-SNAPSHOT/ds-to-spi-annotations-1.0.1-SNAPSHOT% : utils/build-utils/ds-to-spi/ds-to-spi-annotations/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-annotations/.install.pom
utils/build-utils/ds-to-spi/ds-to-spi-annotations/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("utils/build-utils/ds-to-spi/ds-to-spi-annotations");

utils/build-utils/ds-to-spi/ds-to-spi-annotations/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-annotations/.install.jar
utils/build-utils/ds-to-spi/ds-to-spi-annotations/.install.jar : %/.install.jar : %/.install

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-annotations/.install
utils/build-utils/ds-to-spi/ds-to-spi-annotations/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

utils/build-utils/ds-to-spi/ds-to-spi-annotations/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-annotations/.install-doc
utils/build-utils/ds-to-spi/ds-to-spi-annotations/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

utils/build-utils/ds-to-spi/ds-to-spi-annotations/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-annotations/.compile-dependencies utils/build-utils/ds-to-spi/ds-to-spi-annotations/.test-dependencies
utils/build-utils/ds-to-spi/ds-to-spi-annotations/.compile-dependencies :
utils/build-utils/ds-to-spi/ds-to-spi-annotations/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/ds-to-spi-annotations/1.0.1/ds-to-spi-annotations-1.0.1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/ds-to-spi-annotations/1.0.1/ds-to-spi-annotations-1.0.1-% : utils/build-utils/ds-to-spi/ds-to-spi-annotations/.release
	+//

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-annotations/.release
utils/build-utils/ds-to-spi/ds-to-spi-annotations/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

utils/build-utils/ds-to-spi/ds-to-spi-annotations/.release :

clean : utils/build-utils/ds-to-spi/ds-to-spi-annotations/.clean
.PHONY : utils/build-utils/ds-to-spi/ds-to-spi-annotations/.clean
utils/build-utils/ds-to-spi/ds-to-spi-annotations/.clean :
	rm("utils/build-utils/ds-to-spi/ds-to-spi-annotations/target");
