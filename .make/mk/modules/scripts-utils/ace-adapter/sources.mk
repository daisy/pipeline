modules/scripts-utils/ace-adapter/.test modules/scripts-utils/ace-adapter/.install modules/scripts-utils/ace-adapter/.install-doc $(TARGET_DIR)/state/modules/scripts-utils/ace-adapter/modified-since-release_ : \
	modules/scripts-utils/ace-adapter/src/main/resources/META-INF/catalog.xml \
	modules/scripts-utils/ace-adapter/src/main/resources/xml/xproc/library.xpl \
	modules/scripts-utils/ace-adapter/src/main/java/org/daisy/pipeline/epub/ace/Ace.java \
	modules/scripts-utils/ace-adapter/src/main/java/org/daisy/pipeline/epub/ace/impl/AceProvider.java \
	modules/scripts-utils/ace-adapter/src/main/java/org/daisy/pipeline/epub/ace/EpubAccessibilityCheckOption.java \
	modules/scripts-utils/ace-adapter/src/main/java/org/daisy/pipeline/epub/ace/AceFinder.java
modules/scripts-utils/ace-adapter/.test modules/scripts-utils/ace-adapter/.install-doc : \
	modules/scripts-utils/ace-adapter/src/test/resources/logback.xml \
	modules/scripts-utils/ace-adapter/src/test/resources/moby-dick-mo-20120214.OK.epub \
	modules/scripts-utils/ace-adapter/src/test/java/XProcSpecTest.java \
	modules/scripts-utils/ace-adapter/src/test/xprocspec/test.xprocspec
.make/mk/modules/scripts-utils/ace-adapter/sources.mk : \
	modules/scripts-utils/ace-adapter/src \
	modules/scripts-utils/ace-adapter/src/test \
	modules/scripts-utils/ace-adapter/src/test/resources \
	modules/scripts-utils/ace-adapter/src/test/java \
	modules/scripts-utils/ace-adapter/src/test/xprocspec \
	modules/scripts-utils/ace-adapter/src/main \
	modules/scripts-utils/ace-adapter/src/main/resources \
	modules/scripts-utils/ace-adapter/src/main/resources/META-INF \
	modules/scripts-utils/ace-adapter/src/main/resources/xml \
	modules/scripts-utils/ace-adapter/src/main/resources/xml/xproc \
	modules/scripts-utils/ace-adapter/src/main/java \
	modules/scripts-utils/ace-adapter/src/main/java/org \
	modules/scripts-utils/ace-adapter/src/main/java/org/daisy \
	modules/scripts-utils/ace-adapter/src/main/java/org/daisy/pipeline \
	modules/scripts-utils/ace-adapter/src/main/java/org/daisy/pipeline/epub \
	modules/scripts-utils/ace-adapter/src/main/java/org/daisy/pipeline/epub/ace \
	modules/scripts-utils/ace-adapter/src/main/java/org/daisy/pipeline/epub/ace/impl
