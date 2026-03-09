modules/scripts/daisy3-to-epub3/.test modules/scripts/daisy3-to-epub3/.install modules/scripts/daisy3-to-epub3/.install-doc $(TARGET_DIR)/state/modules/scripts/daisy3-to-epub3/modified-since-release_ : \
	modules/scripts/daisy3-to-epub3/src/main/resources/META-INF/catalog.xml \
	modules/scripts/daisy3-to-epub3/src/main/resources/xml/internal/ncx-to-nav.xpl \
	modules/scripts/daisy3-to-epub3/src/main/resources/xml/internal/oebps-to-opf-metadata.xpl \
	modules/scripts/daisy3-to-epub3/src/main/resources/xml/internal/smil-to-dtbook-ids.xsl \
	modules/scripts/daisy3-to-epub3/src/main/resources/xml/internal/opf-to-metadata.xsl \
	modules/scripts/daisy3-to-epub3/src/main/resources/xml/xproc/daisy3-to-epub3.xpl \
	modules/scripts/daisy3-to-epub3/src/main/resources/xml/xproc/convert.xpl \
	modules/scripts/daisy3-to-epub3/src/main/resources/xml/xproc/library.xpl
modules/scripts/daisy3-to-epub3/.test modules/scripts/daisy3-to-epub3/.install-doc : \
	modules/scripts/daisy3-to-epub3/src/test/resources/logback.xml \
	modules/scripts/daisy3-to-epub3/src/test/resources/input/mo0.smil \
	modules/scripts/daisy3-to-epub3/src/test/resources/input/30sec.mp3 \
	modules/scripts/daisy3-to-epub3/src/test/resources/input/navigation.ncx \
	modules/scripts/daisy3-to-epub3/src/test/resources/input/book.opf \
	modules/scripts/daisy3-to-epub3/src/test/resources/input/resources.res \
	modules/scripts/daisy3-to-epub3/src/test/resources/input/minimal.xml \
	modules/scripts/daisy3-to-epub3/src/test/java/XProcSpecTest.java \
	modules/scripts/daisy3-to-epub3/src/test/xprocspec/test_ncx-to-nav.xprocspec \
	modules/scripts/daisy3-to-epub3/src/test/xprocspec/test_daisy3-to-epub3.xprocspec \
	modules/scripts/daisy3-to-epub3/src/test/xprocspec/test_daisy3-to-epub3.script.xprocspec
modules/scripts/daisy3-to-epub3/.install-doc : \
	modules/scripts/daisy3-to-epub3/doc/index.md
.make/mk/modules/scripts/daisy3-to-epub3/sources.mk : \
	modules/scripts/daisy3-to-epub3/src \
	modules/scripts/daisy3-to-epub3/src/test \
	modules/scripts/daisy3-to-epub3/src/test/resources \
	modules/scripts/daisy3-to-epub3/src/test/resources/input \
	modules/scripts/daisy3-to-epub3/src/test/java \
	modules/scripts/daisy3-to-epub3/src/test/xprocspec \
	modules/scripts/daisy3-to-epub3/src/main \
	modules/scripts/daisy3-to-epub3/src/main/resources \
	modules/scripts/daisy3-to-epub3/src/main/resources/META-INF \
	modules/scripts/daisy3-to-epub3/src/main/resources/xml \
	modules/scripts/daisy3-to-epub3/src/main/resources/xml/internal \
	modules/scripts/daisy3-to-epub3/src/main/resources/xml/xproc \
	modules/scripts/daisy3-to-epub3/doc
