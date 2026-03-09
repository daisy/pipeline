utils/build-utils/ds-to-spi/ds-to-spi-runtime/VERSION := 1.2.2-SNAPSHOT

$(TARGET_DIR)/state/utils/build-utils/ds-to-spi/ds-to-spi-runtime/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-runtime/.test
utils/build-utils/ds-to-spi/ds-to-spi-runtime/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

utils/build-utils/ds-to-spi/ds-to-spi-runtime/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/ds-to-spi-runtime/1.2.2-SNAPSHOT/ds-to-spi-runtime-1.2.2-SNAPSHOT.pom : utils/build-utils/ds-to-spi/ds-to-spi-runtime/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/ds-to-spi-runtime/1.2.2-SNAPSHOT/ds-to-spi-runtime-1.2.2-SNAPSHOT% : utils/build-utils/ds-to-spi/ds-to-spi-runtime/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-runtime/.install.pom
utils/build-utils/ds-to-spi/ds-to-spi-runtime/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("utils/build-utils/ds-to-spi/ds-to-spi-runtime");

utils/build-utils/ds-to-spi/ds-to-spi-runtime/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-runtime/.install.jar
utils/build-utils/ds-to-spi/ds-to-spi-runtime/.install.jar : %/.install.jar : %/.install

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-runtime/.install
utils/build-utils/ds-to-spi/ds-to-spi-runtime/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

utils/build-utils/ds-to-spi/ds-to-spi-runtime/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-runtime/.install-javadoc.jar
utils/build-utils/ds-to-spi/ds-to-spi-runtime/.install-javadoc.jar : %/.install-javadoc.jar : %/.install-doc

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-runtime/.install-doc
utils/build-utils/ds-to-spi/ds-to-spi-runtime/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

utils/build-utils/ds-to-spi/ds-to-spi-runtime/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-runtime/.compile-dependencies utils/build-utils/ds-to-spi/ds-to-spi-runtime/.test-dependencies
utils/build-utils/ds-to-spi/ds-to-spi-runtime/.compile-dependencies :
utils/build-utils/ds-to-spi/ds-to-spi-runtime/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/ds-to-spi-runtime/1.2.2/ds-to-spi-runtime-1.2.2.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/ds-to-spi-runtime/1.2.2/ds-to-spi-runtime-1.2.2-% : utils/build-utils/ds-to-spi/ds-to-spi-runtime/.release
	+//

.SECONDARY : utils/build-utils/ds-to-spi/ds-to-spi-runtime/.release
utils/build-utils/ds-to-spi/ds-to-spi-runtime/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

utils/build-utils/ds-to-spi/ds-to-spi-runtime/.release :

clean : utils/build-utils/ds-to-spi/ds-to-spi-runtime/.clean
.PHONY : utils/build-utils/ds-to-spi/ds-to-spi-runtime/.clean
utils/build-utils/ds-to-spi/ds-to-spi-runtime/.clean :
	rm("utils/build-utils/ds-to-spi/ds-to-spi-runtime/target");
