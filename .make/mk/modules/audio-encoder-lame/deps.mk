modules/audio-encoder-lame/VERSION := 3.0.10-SNAPSHOT

$(TARGET_DIR)/state/modules/audio-encoder-lame/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/audio-encoder-lame/modified-since-release_ : modules/audio-encoder-lame/pom.xml \
	$(TARGET_DIR)/state/modules/parent/modified-since-release \
	$(TARGET_DIR)/state/modules/audio-common/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/audio-encoder-lame/.test
modules/audio-encoder-lame/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/audio-encoder-lame/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/audio-encoder-lame/3.0.10-SNAPSHOT/audio-encoder-lame-3.0.10-SNAPSHOT.pom : modules/audio-encoder-lame/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/audio-encoder-lame/3.0.10-SNAPSHOT/audio-encoder-lame-3.0.10-SNAPSHOT% : modules/audio-encoder-lame/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/audio-encoder-lame/.install.pom
modules/audio-encoder-lame/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/audio-encoder-lame");

modules/audio-encoder-lame/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/audio-encoder-lame/.install.jar
modules/audio-encoder-lame/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/audio-encoder-lame/.install
modules/audio-encoder-lame/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/audio-encoder-lame/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/audio-encoder-lame/.install-doc.jar
modules/audio-encoder-lame/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/audio-encoder-lame/.install-xprocdoc.jar
modules/audio-encoder-lame/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/audio-encoder-lame/.install-doc
modules/audio-encoder-lame/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/audio-encoder-lame/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/audio-encoder-lame/.compile-dependencies modules/audio-encoder-lame/.test-dependencies
modules/audio-encoder-lame/.compile-dependencies : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.5-SNAPSHOT/modules-parent-1.15.5-SNAPSHOT.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/audio-common/5.1.9-SNAPSHOT/audio-common-5.1.9-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/common-utils/3.4.1-SNAPSHOT/common-utils-3.4.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/file-utils/5.0.1-SNAPSHOT/file-utils-5.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/fileset-utils/8.0.1-SNAPSHOT/fileset-utils-8.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zip-utils/2.1.11-SNAPSHOT/zip-utils-2.1.11-SNAPSHOT.jar
modules/audio-encoder-lame/.test-dependencies :

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/audio-encoder-lame/3.0.10/audio-encoder-lame-3.0.10.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/audio-encoder-lame/3.0.10/audio-encoder-lame-3.0.10-% : modules/audio-encoder-lame/.release
	+//

.SECONDARY : modules/audio-encoder-lame/.release
modules/audio-encoder-lame/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("audio-encoder-lame");

modules/audio-encoder-lame/.release : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.5/modules-parent-1.15.5.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/audio-common/5.1.9/audio-common-5.1.9.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/common-utils/3.4.1/common-utils-3.4.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/file-utils/5.0.1/file-utils-5.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/fileset-utils/8.0.1/fileset-utils-8.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zip-utils/2.1.11/zip-utils-2.1.11.jar

clean : modules/audio-encoder-lame/.clean
.PHONY : modules/audio-encoder-lame/.clean
modules/audio-encoder-lame/.clean :
	rm("modules/audio-encoder-lame/target");
