modules/scripts/epub-to-daisy/.test modules/scripts/epub-to-daisy/.install modules/scripts/epub-to-daisy/.install-doc $(TARGET_DIR)/state/modules/scripts/epub-to-daisy/modified-since-release_ : \
	modules/scripts/epub-to-daisy/src/main/resources/META-INF/catalog.xml \
	modules/scripts/epub-to-daisy/src/main/resources/xml/epub-to-daisy.xpl \
	modules/scripts/epub-to-daisy/src/main/resources/xml/library.xpl \
	modules/scripts/epub-to-daisy/src/main/resources/xml/epub-to-daisy.script.xpl
modules/scripts/epub-to-daisy/.test modules/scripts/epub-to-daisy/.install-doc : \
	modules/scripts/epub-to-daisy/src/test/resources/logback.xml \
	modules/scripts/epub-to-daisy/src/test/java/XProcSpecTest.java \
	modules/scripts/epub-to-daisy/src/test/xprocspec/test_epub-to-daisy.xprocspec
modules/scripts/epub-to-daisy/.install-doc : \
	modules/scripts/epub-to-daisy/doc/index.md
.make/mk/modules/scripts/epub-to-daisy/sources.mk : \
	modules/scripts/epub-to-daisy/src \
	modules/scripts/epub-to-daisy/src/test \
	modules/scripts/epub-to-daisy/src/test/resources \
	modules/scripts/epub-to-daisy/src/test/java \
	modules/scripts/epub-to-daisy/src/test/xprocspec \
	modules/scripts/epub-to-daisy/src/main \
	modules/scripts/epub-to-daisy/src/main/resources \
	modules/scripts/epub-to-daisy/src/main/resources/META-INF \
	modules/scripts/epub-to-daisy/src/main/resources/xml \
	modules/scripts/epub-to-daisy/doc
