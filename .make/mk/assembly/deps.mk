assembly/VERSION := 1.15.5-SNAPSHOT

$(TARGET_DIR)/state/assembly/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/assembly/modified-since-release_ : assembly/pom.xml \
	$(TARGET_DIR)/state/framework/common-utils/modified-since-release \
	$(TARGET_DIR)/state/framework/calabash-adapter/modified-since-release \
	$(TARGET_DIR)/state/framework/framework-core/modified-since-release \
	$(TARGET_DIR)/state/framework/logging-appender/modified-since-release \
	$(TARGET_DIR)/state/framework/modules-registry/modified-since-release \
	$(TARGET_DIR)/state/framework/pipeline1-adapter/modified-since-release \
	$(TARGET_DIR)/state/framework/saxon-adapter/modified-since-release \
	$(TARGET_DIR)/state/modules/asciimath-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/audio-common/modified-since-release \
	$(TARGET_DIR)/state/modules/audio-encoder-lame/modified-since-release \
	$(TARGET_DIR)/state/modules/common-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/css-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/daisy202-to-epub3/modified-since-release \
	$(TARGET_DIR)/state/modules/daisy202-to-mp3/modified-since-release \
	$(TARGET_DIR)/state/modules/daisy202-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/daisy202-to-daisy3/modified-since-release \
	$(TARGET_DIR)/state/modules/daisy3-to-daisy202/modified-since-release \
	$(TARGET_DIR)/state/modules/daisy3-to-epub3/modified-since-release \
	$(TARGET_DIR)/state/modules/daisy3-to-mp3/modified-since-release \
	$(TARGET_DIR)/state/modules/daisy3-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/dtbook-to-ebraille/modified-since-release \
	$(TARGET_DIR)/state/modules/dtbook-to-epub3/modified-since-release \
	$(TARGET_DIR)/state/modules/dtbook-to-daisy3/modified-since-release \
	$(TARGET_DIR)/state/modules/dtbook-to-html/modified-since-release \
	$(TARGET_DIR)/state/modules/dtbook-to-odt/modified-since-release \
	$(TARGET_DIR)/state/modules/dtbook-to-rtf/modified-since-release \
	$(TARGET_DIR)/state/modules/dtbook-to-zedai/modified-since-release \
	$(TARGET_DIR)/state/modules/dtbook-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/epub-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/epub-to-daisy/modified-since-release \
	$(TARGET_DIR)/state/modules/epub2-to-epub3/modified-since-release \
	$(TARGET_DIR)/state/modules/epub3-to-epub3/modified-since-release \
	$(TARGET_DIR)/state/modules/ace-adapter/modified-since-release \
	$(TARGET_DIR)/state/modules/epubcheck-adapter/modified-since-release \
	$(TARGET_DIR)/state/modules/epub3-to-daisy202/modified-since-release \
	$(TARGET_DIR)/state/modules/epub3-to-daisy3/modified-since-release \
	$(TARGET_DIR)/state/modules/epub3-to-html/modified-since-release \
	$(TARGET_DIR)/state/modules/file-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/fileset-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/html-to-dtbook/modified-since-release \
	$(TARGET_DIR)/state/modules/html-to-epub3/modified-since-release \
	$(TARGET_DIR)/state/modules/html-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/image-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/mathcat-adapter/modified-since-release \
	$(TARGET_DIR)/state/modules/mathml-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/pandoc-adapter/modified-since-release \
	$(TARGET_DIR)/state/modules/smil-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/mediatype-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/metadata-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/nlp-common/modified-since-release \
	$(TARGET_DIR)/state/modules/odf-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/ocr-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/tts-adapter-acapela/modified-since-release \
	$(TARGET_DIR)/state/modules/tts-adapter-azure/modified-since-release \
	$(TARGET_DIR)/state/modules/tts-adapter-cereproc/modified-since-release \
	$(TARGET_DIR)/state/modules/tts-adapter-espeak/modified-since-release \
	$(TARGET_DIR)/state/modules/tts-adapter-google/modified-since-release \
	$(TARGET_DIR)/state/modules/tts-adapter-aws/modified-since-release \
	$(TARGET_DIR)/state/modules/tts-adapter-qfrency/modified-since-release \
	$(TARGET_DIR)/state/modules/tts-common/modified-since-release \
	$(TARGET_DIR)/state/modules/validation-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/word-to-dtbook/modified-since-release \
	$(TARGET_DIR)/state/modules/zedai-to-epub3/modified-since-release \
	$(TARGET_DIR)/state/modules/zedai-to-html/modified-since-release \
	$(TARGET_DIR)/state/modules/zedai-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/zip-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/braille-common/modified-since-release \
	$(TARGET_DIR)/state/modules/braille-css-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/dotify-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/dtbook-to-pef/modified-since-release \
	$(TARGET_DIR)/state/modules/epub3-to-pef/modified-since-release \
	$(TARGET_DIR)/state/modules/html-to-pef/modified-since-release \
	$(TARGET_DIR)/state/modules/libhyphen-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/liblouis-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/pef-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/zedai-to-pef/modified-since-release \
	$(TARGET_DIR)/state/modules/tts-adapter-osx/modified-since-release \
	$(TARGET_DIR)/state/modules/tts-adapter-sapinative/modified-since-release \
	$(TARGET_DIR)/state/framework/webservice/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : assembly/.test
assembly/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

assembly/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/1.15.5-SNAPSHOT/assembly-1.15.5-SNAPSHOT.pom : assembly/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/1.15.5-SNAPSHOT/assembly-1.15.5-SNAPSHOT% : assembly/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : assembly/.install.pom
assembly/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("assembly");

assembly/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : assembly/.install.jar
assembly/.install.jar : %/.install.jar : %/.install

.SECONDARY : assembly/.install
assembly/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

assembly/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : assembly/.install-doc
assembly/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

assembly/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : assembly/.compile-dependencies assembly/.test-dependencies
assembly/.compile-dependencies : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-bom/1.15.7-SNAPSHOT/framework-bom-1.15.7-SNAPSHOT.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-bom/1.15.5-SNAPSHOT/modules-bom-1.15.5-SNAPSHOT.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1-SNAPSHOT/common-utils-6.6.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/calabash-adapter/7.1.1-SNAPSHOT/calabash-adapter-7.1.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/com.xmlcalabash/1.1.20-p20-98-SNAPSHOT/com.xmlcalabash-1.1.20-p20-98-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/saxon-adapter/5.8.2-SNAPSHOT/saxon-adapter-5.8.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules-registry/5.0.2-SNAPSHOT/modules-registry-5.0.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.0.1-SNAPSHOT/framework-core-12.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/logging-appender/2.1.8-SNAPSHOT/logging-appender-2.1.8-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/pipeline1-adapter/1.1.3-SNAPSHOT/pipeline1-adapter-1.1.3-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/asciimath-utils/2.0.2-SNAPSHOT/asciimath-utils-2.0.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-common/9.0.1-SNAPSHOT/tts-common-9.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/webservice/4.0.1-SNAPSHOT/webservice-4.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/audio-common/5.1.9-SNAPSHOT/audio-common-5.1.9-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/common-utils/3.4.1-SNAPSHOT/common-utils-3.4.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/file-utils/5.0.1-SNAPSHOT/file-utils-5.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/fileset-utils/8.0.1-SNAPSHOT/fileset-utils-8.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zip-utils/2.1.11-SNAPSHOT/zip-utils-2.1.11-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/css-utils/8.0.1-SNAPSHOT/css-utils-8.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mediatype-utils/2.1.2-SNAPSHOT/mediatype-utils-2.1.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/audio-encoder-lame/3.0.10-SNAPSHOT/audio-encoder-lame-3.0.10-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy202-to-epub3/2.1.6-SNAPSHOT/daisy202-to-epub3-2.1.6-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy202-utils/1.6.7-SNAPSHOT/daisy202-utils-1.6.7-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/html-utils/6.6.1-SNAPSHOT/html-utils-6.6.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/nlp-common/3.0.6-SNAPSHOT/nlp-common-3.0.6-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/smil-utils/4.0.5-SNAPSHOT/smil-utils-4.0.5-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/validation-utils/2.0.4-SNAPSHOT/validation-utils-2.0.4-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub-utils/2.4.2-SNAPSHOT/epub-utils-2.4.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/odf-utils/1.0.8-SNAPSHOT/odf-utils-1.0.8-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy3-utils/4.2.2-SNAPSHOT/daisy3-utils-4.2.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-utils/6.1.1-SNAPSHOT/dtbook-utils-6.1.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/metadata-utils/2.0.3-SNAPSHOT/metadata-utils-2.0.3-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mathml-utils/1.1.2-SNAPSHOT/mathml-utils-1.1.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epubcheck-adapter/1.1.15-SNAPSHOT/epubcheck-adapter-1.1.15-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/ace-adapter/1.0.13-SNAPSHOT/ace-adapter-1.0.13-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy202-to-mp3/1.1.10-SNAPSHOT/daisy202-to-mp3-1.1.10-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy202-to-daisy3/1.0.12-SNAPSHOT/daisy202-to-daisy3-1.0.12-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/html-to-dtbook/2.0.11-SNAPSHOT/html-to-dtbook-2.0.11-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy3-to-daisy202/2.1.9-SNAPSHOT/daisy3-to-daisy202-2.1.9-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy3-to-epub3/3.1.6-SNAPSHOT/daisy3-to-epub3-3.1.6-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-html/4.1.1-SNAPSHOT/dtbook-to-html-4.1.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-zedai/4.2.1-SNAPSHOT/dtbook-to-zedai-4.2.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-utils/1.3.2-SNAPSHOT/zedai-utils-1.3.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-to-html/2.6.3-SNAPSHOT/zedai-to-html-2.6.3-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy3-to-mp3/1.2.11-SNAPSHOT/daisy3-to-mp3-1.2.11-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-ebraille/1.2.1-SNAPSHOT/dtbook-to-ebraille-1.2.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-common/7.0.1-SNAPSHOT/braille-common-7.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-css-utils/5.0.2-SNAPSHOT/braille-css-utils-5.0.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-epub3/2.8.1-SNAPSHOT/dtbook-to-epub3-2.8.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-to-epub3/2.7.1-SNAPSHOT/zedai-to-epub3-2.7.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/html-to-epub3/2.5.3-SNAPSHOT/html-to-epub3-2.5.3-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-daisy3/4.1.1-SNAPSHOT/dtbook-to-daisy3-4.1.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-odt/2.1.17-SNAPSHOT/dtbook-to-odt-2.1.17-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/image-utils/1.0.9-SNAPSHOT/image-utils-1.0.9-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-rtf/2.0.15-SNAPSHOT/dtbook-to-rtf-2.0.15-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub-to-daisy/1.5.1-SNAPSHOT/epub-to-daisy-1.5.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub2-to-epub3/1.1.6-SNAPSHOT/epub2-to-epub3-1.1.6-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub3-to-epub3/5.1.1-SNAPSHOT/epub3-to-epub3-5.1.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub3-to-daisy202/2.2.12-SNAPSHOT/epub3-to-daisy202-2.2.12-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub3-to-daisy3/1.0.14-SNAPSHOT/epub3-to-daisy3-1.0.14-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub3-to-html/1.0.2-SNAPSHOT/epub3-to-html-1.0.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mathcat-adapter/1.0.2-SNAPSHOT/mathcat-adapter-1.0.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/pandoc-adapter/1.0.1-SNAPSHOT/pandoc-adapter-1.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/ocr-utils/1.0.1-SNAPSHOT/ocr-utils-1.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-acapela/3.1.9-SNAPSHOT/tts-adapter-acapela-3.1.9-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-azure/1.1.5-SNAPSHOT/tts-adapter-azure-1.1.5-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-cereproc/1.1.10-SNAPSHOT/tts-adapter-cereproc-1.1.10-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-espeak/3.0.18-SNAPSHOT/tts-adapter-espeak-3.0.18-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-google/1.3.3-SNAPSHOT/tts-adapter-google-1.3.3-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-aws/1.0.3-SNAPSHOT/tts-adapter-aws-1.0.3-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-qfrency/1.0.14-SNAPSHOT/tts-adapter-qfrency-1.0.14-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/word-to-dtbook/1.1.3-SNAPSHOT/word-to-dtbook-1.1.3-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/dotify-utils/6.5.2-SNAPSHOT/dotify-utils-6.5.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/pef-utils/8.0.2-SNAPSHOT/pef-utils-8.0.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/dtbook-to-pef/13.0.1-SNAPSHOT/dtbook-to-pef-13.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/epub3-to-pef/10.0.1-SNAPSHOT/epub3-to-pef-10.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/html-to-pef/10.0.1-SNAPSHOT/html-to-pef-10.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/libhyphen-utils/3.5.1-SNAPSHOT/libhyphen-utils-3.5.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/liblouis-utils/6.4.1-SNAPSHOT/liblouis-utils-6.4.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/zedai-to-pef/7.1.1-SNAPSHOT/zedai-to-pef-7.1.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/libhyphen-utils/3.5.1-SNAPSHOT/libhyphen-utils-3.5.1-SNAPSHOT-linux.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/liblouis-utils/6.4.1-SNAPSHOT/liblouis-utils-6.4.1-SNAPSHOT-linux.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/libhyphen-utils/3.5.1-SNAPSHOT/libhyphen-utils-3.5.1-SNAPSHOT-mac.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/liblouis-utils/6.4.1-SNAPSHOT/liblouis-utils-6.4.1-SNAPSHOT-mac.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-osx/3.2.2-SNAPSHOT/tts-adapter-osx-3.2.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/liblouis-utils/6.4.1-SNAPSHOT/liblouis-utils-6.4.1-SNAPSHOT-windows.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/libhyphen-utils/3.5.1-SNAPSHOT/libhyphen-utils-3.5.1-SNAPSHOT-windows.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-sapinative/3.2.3-SNAPSHOT/tts-adapter-sapinative-3.2.3-SNAPSHOT.jar
assembly/.test-dependencies : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-bom/1.15.7-SNAPSHOT/framework-bom-1.15.7-SNAPSHOT.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-bom/1.15.5-SNAPSHOT/modules-bom-1.15.5-SNAPSHOT.pom \
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
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/nlp-common/3.0.6-SNAPSHOT/nlp-common-3.0.6-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/liblouis-utils/6.4.1-SNAPSHOT/liblouis-utils-6.4.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-common/7.0.1-SNAPSHOT/braille-common-7.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-css-utils/5.0.2-SNAPSHOT/braille-css-utils-5.0.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/pef-utils/8.0.2-SNAPSHOT/pef-utils-8.0.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/dtbook-to-pef/13.0.1-SNAPSHOT/dtbook-to-pef-13.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-utils/6.1.1-SNAPSHOT/dtbook-utils-6.1.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/validation-utils/2.0.4-SNAPSHOT/validation-utils-2.0.4-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/metadata-utils/2.0.3-SNAPSHOT/metadata-utils-2.0.3-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mathml-utils/1.1.2-SNAPSHOT/mathml-utils-1.1.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-common/9.0.1-SNAPSHOT/tts-common-9.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/audio-common/5.1.9-SNAPSHOT/audio-common-5.1.9-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-epub3/2.8.1-SNAPSHOT/dtbook-to-epub3-2.8.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-zedai/4.2.1-SNAPSHOT/dtbook-to-zedai-4.2.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-utils/1.3.2-SNAPSHOT/zedai-utils-1.3.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub-utils/2.4.2-SNAPSHOT/epub-utils-2.4.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/smil-utils/4.0.5-SNAPSHOT/smil-utils-4.0.5-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/odf-utils/1.0.8-SNAPSHOT/odf-utils-1.0.8-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy3-utils/4.2.2-SNAPSHOT/daisy3-utils-4.2.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epubcheck-adapter/1.1.15-SNAPSHOT/epubcheck-adapter-1.1.15-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/ace-adapter/1.0.13-SNAPSHOT/ace-adapter-1.0.13-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-to-epub3/2.7.1-SNAPSHOT/zedai-to-epub3-2.7.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/html-to-epub3/2.5.3-SNAPSHOT/html-to-epub3-2.5.3-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-to-html/2.6.3-SNAPSHOT/zedai-to-html-2.6.3-SNAPSHOT.jar

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/1.15.5/assembly-1.15.5.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/1.15.5/assembly-1.15.5-% : assembly/.release
	+//

.SECONDARY : assembly/.release
assembly/.release : | .maven-init .group-eval
	+$(EVAL) mvn.releaseDir("$(patsubst %/,%,$(dir $@))");

assembly/.release : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-bom/1.15.7/framework-bom-1.15.7.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-bom/1.15.5/modules-bom-1.15.5.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1/common-utils-6.6.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/calabash-adapter/7.1.1/calabash-adapter-7.1.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/com.xmlcalabash/1.1.20-p20-98/com.xmlcalabash-1.1.20-p20-98.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/saxon-adapter/5.8.2/saxon-adapter-5.8.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules-registry/5.0.2/modules-registry-5.0.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.0.1/framework-core-12.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/logging-appender/2.1.8/logging-appender-2.1.8.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/pipeline1-adapter/1.1.3/pipeline1-adapter-1.1.3.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/asciimath-utils/2.0.2/asciimath-utils-2.0.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-common/9.0.1/tts-common-9.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/webservice/4.0.1/webservice-4.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/audio-common/5.1.9/audio-common-5.1.9.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/common-utils/3.4.1/common-utils-3.4.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/file-utils/5.0.1/file-utils-5.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/fileset-utils/8.0.1/fileset-utils-8.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zip-utils/2.1.11/zip-utils-2.1.11.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/css-utils/8.0.1/css-utils-8.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mediatype-utils/2.1.2/mediatype-utils-2.1.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/audio-encoder-lame/3.0.10/audio-encoder-lame-3.0.10.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy202-to-epub3/2.1.6/daisy202-to-epub3-2.1.6.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy202-utils/1.6.7/daisy202-utils-1.6.7.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/html-utils/6.6.1/html-utils-6.6.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/nlp-common/3.0.6/nlp-common-3.0.6.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/smil-utils/4.0.5/smil-utils-4.0.5.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/validation-utils/2.0.4/validation-utils-2.0.4.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub-utils/2.4.2/epub-utils-2.4.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/odf-utils/1.0.8/odf-utils-1.0.8.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy3-utils/4.2.2/daisy3-utils-4.2.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-utils/6.1.1/dtbook-utils-6.1.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/metadata-utils/2.0.3/metadata-utils-2.0.3.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mathml-utils/1.1.2/mathml-utils-1.1.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epubcheck-adapter/1.1.15/epubcheck-adapter-1.1.15.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/ace-adapter/1.0.13/ace-adapter-1.0.13.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy202-to-mp3/1.1.10/daisy202-to-mp3-1.1.10.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy202-to-daisy3/1.0.12/daisy202-to-daisy3-1.0.12.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/html-to-dtbook/2.0.11/html-to-dtbook-2.0.11.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy3-to-daisy202/2.1.9/daisy3-to-daisy202-2.1.9.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy3-to-epub3/3.1.6/daisy3-to-epub3-3.1.6.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-html/4.1.1/dtbook-to-html-4.1.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-zedai/4.2.1/dtbook-to-zedai-4.2.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-utils/1.3.2/zedai-utils-1.3.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-to-html/2.6.3/zedai-to-html-2.6.3.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/daisy3-to-mp3/1.2.11/daisy3-to-mp3-1.2.11.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-ebraille/1.2.1/dtbook-to-ebraille-1.2.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-common/7.0.1/braille-common-7.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-css-utils/5.0.2/braille-css-utils-5.0.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-epub3/2.8.1/dtbook-to-epub3-2.8.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zedai-to-epub3/2.7.1/zedai-to-epub3-2.7.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/html-to-epub3/2.5.3/html-to-epub3-2.5.3.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/liblouis-utils/6.4.1/liblouis-utils-6.4.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/pef-utils/8.0.2/pef-utils-8.0.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/dtbook-to-pef/13.0.1/dtbook-to-pef-13.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-daisy3/4.1.1/dtbook-to-daisy3-4.1.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-odt/2.1.17/dtbook-to-odt-2.1.17.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/image-utils/1.0.9/image-utils-1.0.9.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/dtbook-to-rtf/2.0.15/dtbook-to-rtf-2.0.15.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub-to-daisy/1.5.1/epub-to-daisy-1.5.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub2-to-epub3/1.1.6/epub2-to-epub3-1.1.6.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub3-to-epub3/5.1.1/epub3-to-epub3-5.1.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub3-to-daisy202/2.2.12/epub3-to-daisy202-2.2.12.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub3-to-daisy3/1.0.14/epub3-to-daisy3-1.0.14.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/epub3-to-html/1.0.2/epub3-to-html-1.0.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/mathcat-adapter/1.0.2/mathcat-adapter-1.0.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/pandoc-adapter/1.0.1/pandoc-adapter-1.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/ocr-utils/1.0.1/ocr-utils-1.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-acapela/3.1.9/tts-adapter-acapela-3.1.9.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-azure/1.1.5/tts-adapter-azure-1.1.5.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-cereproc/1.1.10/tts-adapter-cereproc-1.1.10.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-espeak/3.0.18/tts-adapter-espeak-3.0.18.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-google/1.3.3/tts-adapter-google-1.3.3.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-aws/1.0.3/tts-adapter-aws-1.0.3.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-qfrency/1.0.14/tts-adapter-qfrency-1.0.14.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/word-to-dtbook/1.1.3/word-to-dtbook-1.1.3.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/dotify-utils/6.5.2/dotify-utils-6.5.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/epub3-to-pef/10.0.1/epub3-to-pef-10.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/html-to-pef/10.0.1/html-to-pef-10.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/libhyphen-utils/3.5.1/libhyphen-utils-3.5.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/zedai-to-pef/7.1.1/zedai-to-pef-7.1.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/libhyphen-utils/3.5.1/libhyphen-utils-3.5.1-linux.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/liblouis-utils/6.4.1/liblouis-utils-6.4.1-linux.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/libhyphen-utils/3.5.1/libhyphen-utils-3.5.1-mac.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/liblouis-utils/6.4.1/liblouis-utils-6.4.1-mac.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-osx/3.2.2/tts-adapter-osx-3.2.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/liblouis-utils/6.4.1/liblouis-utils-6.4.1-windows.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/libhyphen-utils/3.5.1/libhyphen-utils-3.5.1-windows.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/tts-adapter-sapinative/3.2.3/tts-adapter-sapinative-3.2.3.jar

clean : assembly/.clean
.PHONY : assembly/.clean
assembly/.clean :
	rm("assembly/target");
