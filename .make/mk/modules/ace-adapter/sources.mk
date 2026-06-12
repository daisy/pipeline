modules/ace-adapter/.test modules/ace-adapter/.install modules/ace-adapter/.install-doc $(TARGET_DIR)/state/modules/ace-adapter/modified-since-release_ : \
	modules/ace-adapter/src/main/resources/META-INF/catalog.xml \
	modules/ace-adapter/src/main/resources/xml/xproc/library.xpl \
	modules/ace-adapter/src/main/java/org/daisy/pipeline/epub/ace/Ace.java \
	modules/ace-adapter/src/main/java/org/daisy/pipeline/epub/ace/impl/AceProvider.java \
	modules/ace-adapter/src/main/java/org/daisy/pipeline/epub/ace/EpubAccessibilityCheckOption.java \
	modules/ace-adapter/src/main/java/org/daisy/pipeline/epub/ace/AceFinder.java
modules/ace-adapter/.test modules/ace-adapter/.install-doc : \
	modules/ace-adapter/src/test/resources/logback.xml \
	modules/ace-adapter/src/test/resources/moby-dick-mo-20120214.OK.epub \
	modules/ace-adapter/src/test/java/XProcSpecTest.java \
	modules/ace-adapter/src/test/xprocspec/test.xprocspec
.make/mk/modules/ace-adapter/sources.mk : \
	modules/ace-adapter/src \
	modules/ace-adapter/src/test \
	modules/ace-adapter/src/test/resources \
	modules/ace-adapter/src/test/java \
	modules/ace-adapter/src/test/xprocspec \
	modules/ace-adapter/src/main \
	modules/ace-adapter/src/main/resources \
	modules/ace-adapter/src/main/resources/META-INF \
	modules/ace-adapter/src/main/resources/xml \
	modules/ace-adapter/src/main/resources/xml/xproc \
	modules/ace-adapter/src/main/java \
	modules/ace-adapter/src/main/java/org \
	modules/ace-adapter/src/main/java/org/daisy \
	modules/ace-adapter/src/main/java/org/daisy/pipeline \
	modules/ace-adapter/src/main/java/org/daisy/pipeline/epub \
	modules/ace-adapter/src/main/java/org/daisy/pipeline/epub/ace \
	modules/ace-adapter/src/main/java/org/daisy/pipeline/epub/ace/impl
