modules/tts-adapter-google/VERSION := 1.3.3-SNAPSHOT

$(TARGET_DIR)/state/modules/tts-adapter-google/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/tts-adapter-google/modified-since-release_ : modules/tts-adapter-google/pom.xml \
	$(TARGET_DIR)/state/modules/parent/modified-since-release \
	$(TARGET_DIR)/state/modules/tts-common/modified-since-release \
	$(TARGET_DIR)/state/utils/build-utils/modules-test-helper/modified-since-release \
	$(TARGET_DIR)/state/framework/logging-appender/modified-since-release \
	$(TARGET_DIR)/state/framework/webservice/modified-since-release \
	$(TARGET_DIR)/state/framework/calabash-adapter/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/tts-adapter-google/.test
modules/tts-adapter-google/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-google/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-google/1.3.3-SNAPSHOT/tts-adapter-google-1.3.3-SNAPSHOT.pom : modules/tts-adapter-google/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-google/1.3.3-SNAPSHOT/tts-adapter-google-1.3.3-SNAPSHOT% : modules/tts-adapter-google/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/tts-adapter-google/.install.pom
modules/tts-adapter-google/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/tts-adapter-google");

modules/tts-adapter-google/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/tts-adapter-google/.install.jar
modules/tts-adapter-google/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/tts-adapter-google/.install
modules/tts-adapter-google/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-google/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/tts-adapter-google/.install-doc.jar
modules/tts-adapter-google/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/tts-adapter-google/.install-xprocdoc.jar
modules/tts-adapter-google/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/tts-adapter-google/.install-doc
modules/tts-adapter-google/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/tts-adapter-google/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/tts-adapter-google/.compile-dependencies modules/tts-adapter-google/.test-dependencies
modules/tts-adapter-google/.compile-dependencies : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.5-SNAPSHOT/modules-parent-1.15.5-SNAPSHOT.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-common/9.0.1-SNAPSHOT/tts-common-9.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/com.xmlcalabash/1.1.20-p20-98-SNAPSHOT/com.xmlcalabash-1.1.20-p20-98-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1-SNAPSHOT/common-utils-6.6.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/calabash-adapter/7.1.1-SNAPSHOT/calabash-adapter-7.1.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/saxon-adapter/5.8.2-SNAPSHOT/saxon-adapter-5.8.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules-registry/5.0.2-SNAPSHOT/modules-registry-5.0.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/webservice/4.0.1-SNAPSHOT/webservice-4.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.0.1-SNAPSHOT/framework-core-12.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/audio-common/5.1.9-SNAPSHOT/audio-common-5.1.9-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/common-utils/3.4.1-SNAPSHOT/common-utils-3.4.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/file-utils/5.0.1-SNAPSHOT/file-utils-5.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/fileset-utils/8.0.1-SNAPSHOT/fileset-utils-8.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zip-utils/2.1.11-SNAPSHOT/zip-utils-2.1.11-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/css-utils/8.0.1-SNAPSHOT/css-utils-8.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mediatype-utils/2.1.2-SNAPSHOT/mediatype-utils-2.1.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/jstyleparser/1.20-p27-SNAPSHOT/jstyleparser-1.20-p27-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/braille/braille-css/1.29.0-SNAPSHOT/braille-css-1.29.0-SNAPSHOT.jar
modules/tts-adapter-google/.test-dependencies : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules-registry/5.0.2-SNAPSHOT/modules-registry-5.0.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1-SNAPSHOT/common-utils-6.6.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-common/9.0.1-SNAPSHOT/tts-common-9.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/com.xmlcalabash/1.1.20-p20-98-SNAPSHOT/com.xmlcalabash-1.1.20-p20-98-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/calabash-adapter/7.1.1-SNAPSHOT/calabash-adapter-7.1.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/saxon-adapter/5.8.2-SNAPSHOT/saxon-adapter-5.8.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/webservice/4.0.1-SNAPSHOT/webservice-4.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.0.1-SNAPSHOT/framework-core-12.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/audio-common/5.1.9-SNAPSHOT/audio-common-5.1.9-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/common-utils/3.4.1-SNAPSHOT/common-utils-3.4.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/file-utils/5.0.1-SNAPSHOT/file-utils-5.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/fileset-utils/8.0.1-SNAPSHOT/fileset-utils-8.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zip-utils/2.1.11-SNAPSHOT/zip-utils-2.1.11-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/css-utils/8.0.1-SNAPSHOT/css-utils-8.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mediatype-utils/2.1.2-SNAPSHOT/mediatype-utils-2.1.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/jstyleparser/1.20-p27-SNAPSHOT/jstyleparser-1.20-p27-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/braille/braille-css/1.29.0-SNAPSHOT/braille-css-1.29.0-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-test-helper/3.0.0-SNAPSHOT/modules-test-helper-3.0.0-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/logging-appender/2.1.8-SNAPSHOT/logging-appender-2.1.8-SNAPSHOT.jar

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-google/1.3.3/tts-adapter-google-1.3.3.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-google/1.3.3/tts-adapter-google-1.3.3-% : modules/tts-adapter-google/.release
	+//

.SECONDARY : modules/tts-adapter-google/.release
modules/tts-adapter-google/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("tts-adapter-google");

modules/tts-adapter-google/.release : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.5/modules-parent-1.15.5.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-common/9.0.1/tts-common-9.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/com.xmlcalabash/1.1.20-p20-98/com.xmlcalabash-1.1.20-p20-98.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1/common-utils-6.6.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/calabash-adapter/7.1.1/calabash-adapter-7.1.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/saxon-adapter/5.8.2/saxon-adapter-5.8.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules-registry/5.0.2/modules-registry-5.0.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/webservice/4.0.1/webservice-4.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.0.1/framework-core-12.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/audio-common/5.1.9/audio-common-5.1.9.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/common-utils/3.4.1/common-utils-3.4.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/file-utils/5.0.1/file-utils-5.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/fileset-utils/8.0.1/fileset-utils-8.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zip-utils/2.1.11/zip-utils-2.1.11.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/css-utils/8.0.1/css-utils-8.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mediatype-utils/2.1.2/mediatype-utils-2.1.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/jstyleparser/1.20-p27/jstyleparser-1.20-p27.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/braille/braille-css/1.29.0/braille-css-1.29.0.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-test-helper/3.0.0/modules-test-helper-3.0.0.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/logging-appender/2.1.8/logging-appender-2.1.8.jar

clean : modules/tts-adapter-google/.clean
.PHONY : modules/tts-adapter-google/.clean
modules/tts-adapter-google/.clean :
	rm("modules/tts-adapter-google/target");
