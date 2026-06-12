modules/epub3-to-daisy3/.test modules/epub3-to-daisy3/.install modules/epub3-to-daisy3/.install-doc $(TARGET_DIR)/state/modules/epub3-to-daisy3/modified-since-release_ : \
	modules/epub3-to-daisy3/src/main/resources/META-INF/catalog.xml \
	modules/epub3-to-daisy3/src/main/resources/xml/epub3-to-daisy3.xpl \
	modules/epub3-to-daisy3/src/main/resources/xml/flatten-headings.xsl \
	modules/epub3-to-daisy3/src/main/resources/xml/page-list-update-links.xpl \
	modules/epub3-to-daisy3/src/main/resources/xml/page-list-update-links.xsl \
	modules/epub3-to-daisy3/src/main/resources/xml/epub3-to-daisy3.script.xpl \
	modules/epub3-to-daisy3/src/main/resources/xml/library.xpl
modules/epub3-to-daisy3/.test modules/epub3-to-daisy3/.install-doc : \
	modules/epub3-to-daisy3/src/test/resources/logback.xml \
	modules/epub3-to-daisy3/src/test/resources/minimal.epub/META-INF/container.xml \
	modules/epub3-to-daisy3/src/test/resources/minimal.epub/mimetype \
	modules/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/nav.xhtml \
	modules/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/chapter.xhtml \
	modules/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/mo/cover.smil \
	modules/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/mo/nav.smil \
	modules/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/mo/chapter.smil \
	modules/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/mo/front.smil \
	modules/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/audio/mock.mp3 \
	modules/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/cover.xhtml \
	modules/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/front.xhtml \
	modules/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/package.opf \
	modules/epub3-to-daisy3/src/test/java/XProcSpecTest.java \
	modules/epub3-to-daisy3/src/test/xprocspec/test_epub3-to-daisy3.xprocspec \
	modules/epub3-to-daisy3/src/test/xprocspec/test_epub3-to-daisy3.script.xprocspec
modules/epub3-to-daisy3/.install-doc : \
	modules/epub3-to-daisy3/doc/index.md
.make/mk/modules/epub3-to-daisy3/sources.mk : \
	modules/epub3-to-daisy3/src \
	modules/epub3-to-daisy3/src/test \
	modules/epub3-to-daisy3/src/test/resources \
	modules/epub3-to-daisy3/src/test/resources/minimal.epub \
	modules/epub3-to-daisy3/src/test/resources/minimal.epub/META-INF \
	modules/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB \
	modules/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/mo \
	modules/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/audio \
	modules/epub3-to-daisy3/src/test/java \
	modules/epub3-to-daisy3/src/test/xprocspec \
	modules/epub3-to-daisy3/src/main \
	modules/epub3-to-daisy3/src/main/resources \
	modules/epub3-to-daisy3/src/main/resources/META-INF \
	modules/epub3-to-daisy3/src/main/resources/xml \
	modules/epub3-to-daisy3/doc
