modules/zedai-to-epub3/VERSION := 2.7.1-SNAPSHOT

$(TARGET_DIR)/state/modules/zedai-to-epub3/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/zedai-to-epub3/modified-since-release_ : modules/zedai-to-epub3/pom.xml \
	$(TARGET_DIR)/state/modules/parent/modified-since-release \
	$(TARGET_DIR)/state/framework/framework-core/modified-since-release \
	$(TARGET_DIR)/state/modules/common-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/css-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/epub-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/html-to-epub3/modified-since-release \
	$(TARGET_DIR)/state/modules/fileset-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/file-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/zedai-to-html/modified-since-release \
	$(TARGET_DIR)/state/modules/zedai-utils/modified-since-release \
	$(TARGET_DIR)/state/framework/modules-registry/modified-since-release \
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

.SECONDARY : modules/zedai-to-epub3/.test
modules/zedai-to-epub3/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/zedai-to-epub3/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-to-epub3/2.7.1-SNAPSHOT/zedai-to-epub3-2.7.1-SNAPSHOT.pom : modules/zedai-to-epub3/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-to-epub3/2.7.1-SNAPSHOT/zedai-to-epub3-2.7.1-SNAPSHOT% : modules/zedai-to-epub3/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/zedai-to-epub3/.install.pom
modules/zedai-to-epub3/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/zedai-to-epub3");

modules/zedai-to-epub3/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/zedai-to-epub3/.install.jar
modules/zedai-to-epub3/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/zedai-to-epub3/.install
modules/zedai-to-epub3/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/zedai-to-epub3/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/zedai-to-epub3/.install-doc.jar
modules/zedai-to-epub3/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/zedai-to-epub3/.install-xprocdoc.jar
modules/zedai-to-epub3/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/zedai-to-epub3/.install-doc
modules/zedai-to-epub3/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/zedai-to-epub3/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/zedai-to-epub3/.compile-dependencies modules/zedai-to-epub3/.test-dependencies
modules/zedai-to-epub3/.compile-dependencies : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.5-SNAPSHOT/modules-parent-1.15.5-SNAPSHOT.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.0.1-SNAPSHOT/framework-core-12.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1-SNAPSHOT/common-utils-6.6.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/common-utils/3.4.1-SNAPSHOT/common-utils-3.4.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/com.xmlcalabash/1.1.20-p20-98-SNAPSHOT/com.xmlcalabash-1.1.20-p20-98-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/calabash-adapter/7.1.1-SNAPSHOT/calabash-adapter-7.1.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/saxon-adapter/5.8.2-SNAPSHOT/saxon-adapter-5.8.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules-registry/5.0.2-SNAPSHOT/modules-registry-5.0.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/css-utils/8.0.1-SNAPSHOT/css-utils-8.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/file-utils/5.0.1-SNAPSHOT/file-utils-5.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/fileset-utils/8.0.1-SNAPSHOT/fileset-utils-8.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zip-utils/2.1.11-SNAPSHOT/zip-utils-2.1.11-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mediatype-utils/2.1.2-SNAPSHOT/mediatype-utils-2.1.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/webservice/4.0.1-SNAPSHOT/webservice-4.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/jstyleparser/1.20-p27-SNAPSHOT/jstyleparser-1.20-p27-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/braille/braille-css/1.29.0-SNAPSHOT/braille-css-1.29.0-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub-utils/2.4.2-SNAPSHOT/epub-utils-2.4.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/html-utils/6.6.1-SNAPSHOT/html-utils-6.6.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/nlp-common/3.0.6-SNAPSHOT/nlp-common-3.0.6-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/smil-utils/4.0.5-SNAPSHOT/smil-utils-4.0.5-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/odf-utils/1.0.8-SNAPSHOT/odf-utils-1.0.8-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy3-utils/4.2.2-SNAPSHOT/daisy3-utils-4.2.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-utils/6.1.1-SNAPSHOT/dtbook-utils-6.1.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/validation-utils/2.0.4-SNAPSHOT/validation-utils-2.0.4-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/metadata-utils/2.0.3-SNAPSHOT/metadata-utils-2.0.3-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mathml-utils/1.1.2-SNAPSHOT/mathml-utils-1.1.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-common/9.0.1-SNAPSHOT/tts-common-9.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/audio-common/5.1.9-SNAPSHOT/audio-common-5.1.9-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epubcheck-adapter/1.1.15-SNAPSHOT/epubcheck-adapter-1.1.15-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/ace-adapter/1.0.13-SNAPSHOT/ace-adapter-1.0.13-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-css-utils/5.0.2-SNAPSHOT/braille-css-utils-5.0.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-common/7.0.1-SNAPSHOT/braille-common-7.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/html-to-epub3/2.5.3-SNAPSHOT/html-to-epub3-2.5.3-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-to-html/2.6.3-SNAPSHOT/zedai-to-html-2.6.3-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-utils/1.3.2-SNAPSHOT/zedai-utils-1.3.2-SNAPSHOT.jar
modules/zedai-to-epub3/.test-dependencies : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules-registry/5.0.2-SNAPSHOT/modules-registry-5.0.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1-SNAPSHOT/common-utils-6.6.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/html-utils/6.6.1-SNAPSHOT/html-utils-6.6.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/saxon-adapter/5.8.2-SNAPSHOT/saxon-adapter-5.8.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/com.xmlcalabash/1.1.20-p20-98-SNAPSHOT/com.xmlcalabash-1.1.20-p20-98-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/file-utils/5.0.1-SNAPSHOT/file-utils-5.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/calabash-adapter/7.1.1-SNAPSHOT/calabash-adapter-7.1.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/common-utils/3.4.1-SNAPSHOT/common-utils-3.4.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/fileset-utils/8.0.1-SNAPSHOT/fileset-utils-8.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zip-utils/2.1.11-SNAPSHOT/zip-utils-2.1.11-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mediatype-utils/2.1.2-SNAPSHOT/mediatype-utils-2.1.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/css-utils/8.0.1-SNAPSHOT/css-utils-8.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.0.1-SNAPSHOT/framework-core-12.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/webservice/4.0.1-SNAPSHOT/webservice-4.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/jstyleparser/1.20-p27-SNAPSHOT/jstyleparser-1.20-p27-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/braille/braille-css/1.29.0-SNAPSHOT/braille-css-1.29.0-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/nlp-common/3.0.6-SNAPSHOT/nlp-common-3.0.6-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-test-helper/3.0.0-SNAPSHOT/modules-test-helper-3.0.0-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/logging-appender/2.1.8-SNAPSHOT/logging-appender-2.1.8-SNAPSHOT.jar

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-to-epub3/2.7.1/zedai-to-epub3-2.7.1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-to-epub3/2.7.1/zedai-to-epub3-2.7.1-% : modules/zedai-to-epub3/.release
	+//

.SECONDARY : modules/zedai-to-epub3/.release
modules/zedai-to-epub3/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("zedai-to-epub3");

modules/zedai-to-epub3/.release : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.5/modules-parent-1.15.5.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.0.1/framework-core-12.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1/common-utils-6.6.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/common-utils/3.4.1/common-utils-3.4.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/com.xmlcalabash/1.1.20-p20-98/com.xmlcalabash-1.1.20-p20-98.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/calabash-adapter/7.1.1/calabash-adapter-7.1.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/saxon-adapter/5.8.2/saxon-adapter-5.8.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules-registry/5.0.2/modules-registry-5.0.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/css-utils/8.0.1/css-utils-8.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/file-utils/5.0.1/file-utils-5.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/fileset-utils/8.0.1/fileset-utils-8.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zip-utils/2.1.11/zip-utils-2.1.11.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mediatype-utils/2.1.2/mediatype-utils-2.1.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/webservice/4.0.1/webservice-4.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/jstyleparser/1.20-p27/jstyleparser-1.20-p27.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/braille/braille-css/1.29.0/braille-css-1.29.0.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub-utils/2.4.2/epub-utils-2.4.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/html-utils/6.6.1/html-utils-6.6.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/nlp-common/3.0.6/nlp-common-3.0.6.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/smil-utils/4.0.5/smil-utils-4.0.5.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/odf-utils/1.0.8/odf-utils-1.0.8.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy3-utils/4.2.2/daisy3-utils-4.2.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-utils/6.1.1/dtbook-utils-6.1.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/validation-utils/2.0.4/validation-utils-2.0.4.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/metadata-utils/2.0.3/metadata-utils-2.0.3.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mathml-utils/1.1.2/mathml-utils-1.1.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-common/9.0.1/tts-common-9.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/audio-common/5.1.9/audio-common-5.1.9.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epubcheck-adapter/1.1.15/epubcheck-adapter-1.1.15.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/ace-adapter/1.0.13/ace-adapter-1.0.13.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-css-utils/5.0.2/braille-css-utils-5.0.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-common/7.0.1/braille-common-7.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/html-to-epub3/2.5.3/html-to-epub3-2.5.3.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-to-html/2.6.3/zedai-to-html-2.6.3.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-utils/1.3.2/zedai-utils-1.3.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-test-helper/3.0.0/modules-test-helper-3.0.0.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/logging-appender/2.1.8/logging-appender-2.1.8.jar

clean : modules/zedai-to-epub3/.clean
.PHONY : modules/zedai-to-epub3/.clean
modules/zedai-to-epub3/.clean :
	rm("modules/zedai-to-epub3/target");
