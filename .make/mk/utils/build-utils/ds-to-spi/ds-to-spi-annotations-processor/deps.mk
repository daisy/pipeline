utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/VERSION := 1.1.5-SNAPSHOT

$(TARGET_DIR)/state/utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.test
utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/ds-to-spi-annotations-processor/1.1.5-SNAPSHOT/ds-to-spi-annotations-processor-1.1.5-SNAPSHOT.pom : utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/ds-to-spi-annotations-processor/1.1.5-SNAPSHOT/ds-to-spi-annotations-processor-1.1.5-SNAPSHOT% : utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.install.pom
utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor");

utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.install.jar
utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.install.jar : %/.install.jar : %/.install

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.install
utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.install-doc
utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.compile-dependencies utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.test-dependencies
utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.compile-dependencies :
utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/ds-to-spi-annotations-processor/1.1.5/ds-to-spi-annotations-processor-1.1.5.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/ds-to-spi-annotations-processor/1.1.5/ds-to-spi-annotations-processor-1.1.5-% : utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.release
	+//

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.release
utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.release :

clean : utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.clean
.PHONY : utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.clean
utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/.clean :
	rm("utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/target");
