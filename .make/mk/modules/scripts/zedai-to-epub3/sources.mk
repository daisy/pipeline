modules/scripts/zedai-to-epub3/.test modules/scripts/zedai-to-epub3/.install modules/scripts/zedai-to-epub3/.install-doc $(TARGET_DIR)/state/modules/scripts/zedai-to-epub3/modified-since-release_ : \
	modules/scripts/zedai-to-epub3/src/main/resources/META-INF/catalog.xml \
	modules/scripts/zedai-to-epub3/src/main/resources/xml/xproc/zedai-to-epub3.convert.xpl \
	modules/scripts/zedai-to-epub3/src/main/resources/xml/xproc/zedai-to-epub3.xpl \
	modules/scripts/zedai-to-epub3/src/main/resources/xml/xproc/zedai-to-opf-metadata.xpl \
	modules/scripts/zedai-to-epub3/src/main/resources/xml/xproc/library.xpl \
	modules/scripts/zedai-to-epub3/src/main/resources/xml/xslt/zedai-to-opf-metadata.xsl
modules/scripts/zedai-to-epub3/.test modules/scripts/zedai-to-epub3/.install-doc : \
	modules/scripts/zedai-to-epub3/src/test/resources/logback.xml \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/alice.xml \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice25a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice33a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice09a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice29a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice13a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice05a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice04a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice12a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice28a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice08a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice32a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice24a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice39a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice42a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice15a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice03a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice23a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice35a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice19a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice18a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice34a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice22a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice02a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice14a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice38a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice01a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice17a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice40a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice37a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice21a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice20a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice36a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice41a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice16a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice31a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice27a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice07a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice11a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice10a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice06a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice26a.png \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images/alice30a.png \
	modules/scripts/zedai-to-epub3/src/test/java/XProcSpecTest.java \
	modules/scripts/zedai-to-epub3/src/test/xprocspec/test_zedai-to-opf-metadata.xprocspec \
	modules/scripts/zedai-to-epub3/src/test/xprocspec/test_zedai-to-epub3.script.xprocspec \
	modules/scripts/zedai-to-epub3/src/test/xprocspec/test_zedai-to-epub3.xprocspec
modules/scripts/zedai-to-epub3/.install-doc : \
	modules/scripts/zedai-to-epub3/doc/index.md
.make/mk/modules/scripts/zedai-to-epub3/sources.mk : \
	modules/scripts/zedai-to-epub3/src \
	modules/scripts/zedai-to-epub3/src/test \
	modules/scripts/zedai-to-epub3/src/test/resources \
	modules/scripts/zedai-to-epub3/src/test/resources/resources \
	modules/scripts/zedai-to-epub3/src/test/resources/resources/images \
	modules/scripts/zedai-to-epub3/src/test/java \
	modules/scripts/zedai-to-epub3/src/test/xprocspec \
	modules/scripts/zedai-to-epub3/src/main \
	modules/scripts/zedai-to-epub3/src/main/resources \
	modules/scripts/zedai-to-epub3/src/main/resources/META-INF \
	modules/scripts/zedai-to-epub3/src/main/resources/xml \
	modules/scripts/zedai-to-epub3/src/main/resources/xml/xproc \
	modules/scripts/zedai-to-epub3/src/main/resources/xml/xslt \
	modules/scripts/zedai-to-epub3/doc
