modules/zedai-to-epub3/.test modules/zedai-to-epub3/.install modules/zedai-to-epub3/.install-doc $(TARGET_DIR)/state/modules/zedai-to-epub3/modified-since-release_ : \
	modules/zedai-to-epub3/src/main/resources/META-INF/catalog.xml \
	modules/zedai-to-epub3/src/main/resources/xml/xproc/zedai-to-epub3.convert.xpl \
	modules/zedai-to-epub3/src/main/resources/xml/xproc/zedai-to-epub3.xpl \
	modules/zedai-to-epub3/src/main/resources/xml/xproc/zedai-to-opf-metadata.xpl \
	modules/zedai-to-epub3/src/main/resources/xml/xproc/library.xpl \
	modules/zedai-to-epub3/src/main/resources/xml/xslt/zedai-to-opf-metadata.xsl
modules/zedai-to-epub3/.test modules/zedai-to-epub3/.install-doc : \
	modules/zedai-to-epub3/src/test/resources/logback.xml \
	modules/zedai-to-epub3/src/test/resources/resources/alice.xml \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice25a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice33a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice09a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice29a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice13a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice05a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice04a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice12a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice28a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice08a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice32a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice24a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice39a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice42a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice15a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice03a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice23a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice35a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice19a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice18a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice34a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice22a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice02a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice14a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice38a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice01a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice17a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice40a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice37a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice21a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice20a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice36a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice41a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice16a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice31a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice27a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice07a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice11a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice10a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice06a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice26a.png \
	modules/zedai-to-epub3/src/test/resources/resources/images/alice30a.png \
	modules/zedai-to-epub3/src/test/java/XProcSpecTest.java \
	modules/zedai-to-epub3/src/test/xprocspec/test_zedai-to-opf-metadata.xprocspec \
	modules/zedai-to-epub3/src/test/xprocspec/test_zedai-to-epub3.script.xprocspec \
	modules/zedai-to-epub3/src/test/xprocspec/test_zedai-to-epub3.xprocspec
modules/zedai-to-epub3/.install-doc : \
	modules/zedai-to-epub3/doc/index.md
.make/mk/modules/zedai-to-epub3/sources.mk : \
	modules/zedai-to-epub3/src \
	modules/zedai-to-epub3/src/test \
	modules/zedai-to-epub3/src/test/resources \
	modules/zedai-to-epub3/src/test/resources/resources \
	modules/zedai-to-epub3/src/test/resources/resources/images \
	modules/zedai-to-epub3/src/test/java \
	modules/zedai-to-epub3/src/test/xprocspec \
	modules/zedai-to-epub3/src/main \
	modules/zedai-to-epub3/src/main/resources \
	modules/zedai-to-epub3/src/main/resources/META-INF \
	modules/zedai-to-epub3/src/main/resources/xml \
	modules/zedai-to-epub3/src/main/resources/xml/xproc \
	modules/zedai-to-epub3/src/main/resources/xml/xslt \
	modules/zedai-to-epub3/doc
