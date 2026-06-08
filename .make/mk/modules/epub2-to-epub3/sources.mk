modules/epub2-to-epub3/.test modules/epub2-to-epub3/.install modules/epub2-to-epub3/.install-doc $(TARGET_DIR)/state/modules/epub2-to-epub3/modified-since-release_ : \
	modules/epub2-to-epub3/src/main/resources/META-INF/catalog.xml \
	modules/epub2-to-epub3/src/main/resources/xml/epub2-to-epub3.xpl \
	modules/epub2-to-epub3/src/main/resources/xml/epub2-to-epub3.script.xpl \
	modules/epub2-to-epub3/src/main/resources/xml/library.xpl
modules/epub2-to-epub3/.test modules/epub2-to-epub3/.install-doc : \
	modules/epub2-to-epub3/src/test/resources/logback.xml \
	modules/epub2-to-epub3/src/test/resources/minimal.epub/META-INF/container.xml \
	modules/epub2-to-epub3/src/test/resources/minimal.epub/mimetype \
	modules/epub2-to-epub3/src/test/resources/minimal.epub/EPUB/chapter.xhtml \
	modules/epub2-to-epub3/src/test/resources/minimal.epub/EPUB/cover.xhtml \
	modules/epub2-to-epub3/src/test/resources/minimal.epub/EPUB/front.xhtml \
	modules/epub2-to-epub3/src/test/resources/minimal.epub/EPUB/toc.ncx \
	modules/epub2-to-epub3/src/test/resources/minimal.epub/EPUB/package.opf \
	modules/epub2-to-epub3/src/test/java/XProcSpecTest.java \
	modules/epub2-to-epub3/src/test/xprocspec/test_epub2-to-epub3.script.xprocspec \
	modules/epub2-to-epub3/src/test/xprocspec/test_epub2-to-epub3.xprocspec
modules/epub2-to-epub3/.install-doc : \
	modules/epub2-to-epub3/doc/index.md
.make/mk/modules/epub2-to-epub3/sources.mk : \
	modules/epub2-to-epub3/src \
	modules/epub2-to-epub3/src/test \
	modules/epub2-to-epub3/src/test/resources \
	modules/epub2-to-epub3/src/test/resources/minimal.epub \
	modules/epub2-to-epub3/src/test/resources/minimal.epub/META-INF \
	modules/epub2-to-epub3/src/test/resources/minimal.epub/EPUB \
	modules/epub2-to-epub3/src/test/java \
	modules/epub2-to-epub3/src/test/xprocspec \
	modules/epub2-to-epub3/src/main \
	modules/epub2-to-epub3/src/main/resources \
	modules/epub2-to-epub3/src/main/resources/META-INF \
	modules/epub2-to-epub3/src/main/resources/xml \
	modules/epub2-to-epub3/doc
