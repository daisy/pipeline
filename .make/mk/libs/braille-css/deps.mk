libs/braille-css/VERSION := 1.28.0

$(TARGET_DIR)/state/libs/braille-css/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : libs/braille-css/.test
libs/braille-css/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

libs/braille-css/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/braille/braille-css/1.28.0/braille-css-1.28.0.pom : libs/braille-css/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/braille/braille-css/1.28.0/braille-css-1.28.0% : libs/braille-css/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : libs/braille-css/.install.pom
libs/braille-css/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("libs/braille-css");

libs/braille-css/.install.pom : %/.install.pom : | %/.compile-dependencies %/.test-dependencies

.SECONDARY : libs/braille-css/.install.jar
libs/braille-css/.install.jar : %/.install.jar : %/.install

.SECONDARY : libs/braille-css/.install
libs/braille-css/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

libs/braille-css/.install : %/.install : | %/.compile-dependencies %/.test-dependencies

.SECONDARY : libs/braille-css/.install-doc
libs/braille-css/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

libs/braille-css/.install-doc : %/.install-doc : | %/.compile-dependencies %/.test-dependencies

.SECONDARY : libs/braille-css/.compile-dependencies libs/braille-css/.test-dependencies
libs/braille-css/.compile-dependencies :
libs/braille-css/.test-dependencies :

.SECONDARY : libs/braille-css/.release

clean : libs/braille-css/.clean
.PHONY : libs/braille-css/.clean
libs/braille-css/.clean :
	rm("libs/braille-css/target");
