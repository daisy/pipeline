assembly/VERSION := 1.15.5

$(TARGET_DIR)/state/assembly/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : assembly/.test
assembly/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

assembly/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/1.15.5/assembly-1.15.5.pom : assembly/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/1.15.5/assembly-1.15.5% : assembly/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : assembly/.install.pom
assembly/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("assembly");

assembly/.install.pom : %/.install.pom : | %/.compile-dependencies %/.test-dependencies

.SECONDARY : assembly/.install.jar
assembly/.install.jar : %/.install.jar : %/.install

.SECONDARY : assembly/.install
assembly/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

assembly/.install : %/.install : | %/.compile-dependencies %/.test-dependencies

.SECONDARY : assembly/.install-doc
assembly/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

assembly/.install-doc : %/.install-doc : | %/.compile-dependencies %/.test-dependencies

.SECONDARY : assembly/.compile-dependencies assembly/.test-dependencies
assembly/.compile-dependencies :
assembly/.test-dependencies :

.SECONDARY : assembly/.release

clean : assembly/.clean
.PHONY : assembly/.clean
assembly/.clean :
	rm("assembly/target");
