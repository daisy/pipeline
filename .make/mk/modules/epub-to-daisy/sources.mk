modules/epub-to-daisy/.test modules/epub-to-daisy/.install modules/epub-to-daisy/.install-doc $(TARGET_DIR)/state/modules/epub-to-daisy/modified-since-release_ : \
	modules/epub-to-daisy/src/main/resources/META-INF/catalog.xml \
	modules/epub-to-daisy/src/main/resources/xml/epub-to-daisy.xpl \
	modules/epub-to-daisy/src/main/resources/xml/library.xpl \
	modules/epub-to-daisy/src/main/resources/xml/epub-to-daisy.script.xpl
modules/epub-to-daisy/.test modules/epub-to-daisy/.install-doc : \
	modules/epub-to-daisy/src/test/resources/logback.xml \
	modules/epub-to-daisy/src/test/java/XProcSpecTest.java \
	modules/epub-to-daisy/src/test/xprocspec/test_epub-to-daisy.xprocspec
modules/epub-to-daisy/.install-doc : \
	modules/epub-to-daisy/doc/index.md
.make/mk/modules/epub-to-daisy/sources.mk : \
	modules/epub-to-daisy/src \
	modules/epub-to-daisy/src/test \
	modules/epub-to-daisy/src/test/resources \
	modules/epub-to-daisy/src/test/java \
	modules/epub-to-daisy/src/test/xprocspec \
	modules/epub-to-daisy/src/main \
	modules/epub-to-daisy/src/main/resources \
	modules/epub-to-daisy/src/main/resources/META-INF \
	modules/epub-to-daisy/src/main/resources/xml \
	modules/epub-to-daisy/doc
