modules/scripts/epub3-to-daisy3/.test modules/scripts/epub3-to-daisy3/.install modules/scripts/epub3-to-daisy3/.install-doc $(TARGET_DIR)/state/modules/scripts/epub3-to-daisy3/modified-since-release_ : \
	modules/scripts/epub3-to-daisy3/src/main/resources/META-INF/catalog.xml \
	modules/scripts/epub3-to-daisy3/src/main/resources/xml/epub3-to-daisy3.xpl \
	modules/scripts/epub3-to-daisy3/src/main/resources/xml/flatten-headings.xsl \
	modules/scripts/epub3-to-daisy3/src/main/resources/xml/page-list-update-links.xpl \
	modules/scripts/epub3-to-daisy3/src/main/resources/xml/page-list-update-links.xsl \
	modules/scripts/epub3-to-daisy3/src/main/resources/xml/epub3-to-daisy3.script.xpl \
	modules/scripts/epub3-to-daisy3/src/main/resources/xml/library.xpl
modules/scripts/epub3-to-daisy3/.test modules/scripts/epub3-to-daisy3/.install-doc : \
	modules/scripts/epub3-to-daisy3/src/test/resources/logback.xml \
	modules/scripts/epub3-to-daisy3/src/test/resources/minimal.epub/META-INF/container.xml \
	modules/scripts/epub3-to-daisy3/src/test/resources/minimal.epub/mimetype \
	modules/scripts/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/nav.xhtml \
	modules/scripts/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/chapter.xhtml \
	modules/scripts/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/mo/cover.smil \
	modules/scripts/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/mo/nav.smil \
	modules/scripts/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/mo/chapter.smil \
	modules/scripts/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/mo/front.smil \
	modules/scripts/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/audio/mock.mp3 \
	modules/scripts/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/cover.xhtml \
	modules/scripts/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/front.xhtml \
	modules/scripts/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/package.opf \
	modules/scripts/epub3-to-daisy3/src/test/java/XProcSpecTest.java \
	modules/scripts/epub3-to-daisy3/src/test/xprocspec/test_epub3-to-daisy3.xprocspec \
	modules/scripts/epub3-to-daisy3/src/test/xprocspec/test_epub3-to-daisy3.script.xprocspec
modules/scripts/epub3-to-daisy3/.install-doc : \
	modules/scripts/epub3-to-daisy3/doc/index.md
.make/mk/modules/scripts/epub3-to-daisy3/sources.mk : \
	modules/scripts/epub3-to-daisy3/src \
	modules/scripts/epub3-to-daisy3/src/test \
	modules/scripts/epub3-to-daisy3/src/test/resources \
	modules/scripts/epub3-to-daisy3/src/test/resources/minimal.epub \
	modules/scripts/epub3-to-daisy3/src/test/resources/minimal.epub/META-INF \
	modules/scripts/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB \
	modules/scripts/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/mo \
	modules/scripts/epub3-to-daisy3/src/test/resources/minimal.epub/EPUB/audio \
	modules/scripts/epub3-to-daisy3/src/test/java \
	modules/scripts/epub3-to-daisy3/src/test/xprocspec \
	modules/scripts/epub3-to-daisy3/src/main \
	modules/scripts/epub3-to-daisy3/src/main/resources \
	modules/scripts/epub3-to-daisy3/src/main/resources/META-INF \
	modules/scripts/epub3-to-daisy3/src/main/resources/xml \
	modules/scripts/epub3-to-daisy3/doc
