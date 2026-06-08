modules/daisy3-to-epub3/.test modules/daisy3-to-epub3/.install modules/daisy3-to-epub3/.install-doc $(TARGET_DIR)/state/modules/daisy3-to-epub3/modified-since-release_ : \
	modules/daisy3-to-epub3/src/main/resources/META-INF/catalog.xml \
	modules/daisy3-to-epub3/src/main/resources/xml/internal/ncx-to-nav.xpl \
	modules/daisy3-to-epub3/src/main/resources/xml/internal/oebps-to-opf-metadata.xpl \
	modules/daisy3-to-epub3/src/main/resources/xml/internal/smil-to-dtbook-ids.xsl \
	modules/daisy3-to-epub3/src/main/resources/xml/internal/opf-to-metadata.xsl \
	modules/daisy3-to-epub3/src/main/resources/xml/xproc/daisy3-to-epub3.xpl \
	modules/daisy3-to-epub3/src/main/resources/xml/xproc/convert.xpl \
	modules/daisy3-to-epub3/src/main/resources/xml/xproc/library.xpl
modules/daisy3-to-epub3/.test modules/daisy3-to-epub3/.install-doc : \
	modules/daisy3-to-epub3/src/test/resources/logback.xml \
	modules/daisy3-to-epub3/src/test/resources/input/mo0.smil \
	modules/daisy3-to-epub3/src/test/resources/input/30sec.mp3 \
	modules/daisy3-to-epub3/src/test/resources/input/navigation.ncx \
	modules/daisy3-to-epub3/src/test/resources/input/book.opf \
	modules/daisy3-to-epub3/src/test/resources/input/resources.res \
	modules/daisy3-to-epub3/src/test/resources/input/minimal.xml \
	modules/daisy3-to-epub3/src/test/java/XProcSpecTest.java \
	modules/daisy3-to-epub3/src/test/xprocspec/test_ncx-to-nav.xprocspec \
	modules/daisy3-to-epub3/src/test/xprocspec/test_daisy3-to-epub3.xprocspec \
	modules/daisy3-to-epub3/src/test/xprocspec/test_daisy3-to-epub3.script.xprocspec
modules/daisy3-to-epub3/.install-doc : \
	modules/daisy3-to-epub3/doc/index.md
.make/mk/modules/daisy3-to-epub3/sources.mk : \
	modules/daisy3-to-epub3/src \
	modules/daisy3-to-epub3/src/test \
	modules/daisy3-to-epub3/src/test/resources \
	modules/daisy3-to-epub3/src/test/resources/input \
	modules/daisy3-to-epub3/src/test/java \
	modules/daisy3-to-epub3/src/test/xprocspec \
	modules/daisy3-to-epub3/src/main \
	modules/daisy3-to-epub3/src/main/resources \
	modules/daisy3-to-epub3/src/main/resources/META-INF \
	modules/daisy3-to-epub3/src/main/resources/xml \
	modules/daisy3-to-epub3/src/main/resources/xml/internal \
	modules/daisy3-to-epub3/src/main/resources/xml/xproc \
	modules/daisy3-to-epub3/doc
