utils/build-utils/archetypes/basic-module/VERSION := 1.0.0-SNAPSHOT

$(TARGET_DIR)/state/utils/build-utils/archetypes/basic-module/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : utils/build-utils/archetypes/basic-module/.test
utils/build-utils/archetypes/basic-module/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

utils/build-utils/archetypes/basic-module/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/archetypes/basic-module/1.0.0-SNAPSHOT/basic-module-1.0.0-SNAPSHOT.pom : utils/build-utils/archetypes/basic-module/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/archetypes/basic-module/1.0.0-SNAPSHOT/basic-module-1.0.0-SNAPSHOT% : utils/build-utils/archetypes/basic-module/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : utils/build-utils/archetypes/basic-module/.install.pom
utils/build-utils/archetypes/basic-module/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("utils/build-utils/archetypes/basic-module");

utils/build-utils/archetypes/basic-module/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : utils/build-utils/archetypes/basic-module/.install-doc
utils/build-utils/archetypes/basic-module/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

utils/build-utils/archetypes/basic-module/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : utils/build-utils/archetypes/basic-module/.compile-dependencies utils/build-utils/archetypes/basic-module/.test-dependencies
utils/build-utils/archetypes/basic-module/.compile-dependencies :
utils/build-utils/archetypes/basic-module/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/archetypes/basic-module/1.0.0/basic-module-1.0.0.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/archetypes/basic-module/1.0.0/basic-module-1.0.0-% : utils/build-utils/archetypes/basic-module/.release
	+//

.SECONDARY : utils/build-utils/archetypes/basic-module/.release
utils/build-utils/archetypes/basic-module/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

utils/build-utils/archetypes/basic-module/.release :

clean : utils/build-utils/archetypes/basic-module/.clean
.PHONY : utils/build-utils/archetypes/basic-module/.clean
utils/build-utils/archetypes/basic-module/.clean :
	rm("utils/build-utils/archetypes/basic-module/target");
