modules/scripts/epub2-to-epub3/.test modules/scripts/epub2-to-epub3/.install modules/scripts/epub2-to-epub3/.install-doc $(TARGET_DIR)/state/modules/scripts/epub2-to-epub3/modified-since-release_ : \
	modules/scripts/epub2-to-epub3/src/main/resources/META-INF/catalog.xml \
	modules/scripts/epub2-to-epub3/src/main/resources/xml/epub2-to-epub3.xpl \
	modules/scripts/epub2-to-epub3/src/main/resources/xml/epub2-to-epub3.script.xpl \
	modules/scripts/epub2-to-epub3/src/main/resources/xml/library.xpl
modules/scripts/epub2-to-epub3/.test modules/scripts/epub2-to-epub3/.install-doc : \
	modules/scripts/epub2-to-epub3/src/test/resources/logback.xml \
	modules/scripts/epub2-to-epub3/src/test/resources/minimal.epub/META-INF/container.xml \
	modules/scripts/epub2-to-epub3/src/test/resources/minimal.epub/mimetype \
	modules/scripts/epub2-to-epub3/src/test/resources/minimal.epub/EPUB/chapter.xhtml \
	modules/scripts/epub2-to-epub3/src/test/resources/minimal.epub/EPUB/cover.xhtml \
	modules/scripts/epub2-to-epub3/src/test/resources/minimal.epub/EPUB/front.xhtml \
	modules/scripts/epub2-to-epub3/src/test/resources/minimal.epub/EPUB/toc.ncx \
	modules/scripts/epub2-to-epub3/src/test/resources/minimal.epub/EPUB/package.opf \
	modules/scripts/epub2-to-epub3/src/test/java/XProcSpecTest.java \
	modules/scripts/epub2-to-epub3/src/test/xprocspec/test_epub2-to-epub3.script.xprocspec \
	modules/scripts/epub2-to-epub3/src/test/xprocspec/test_epub2-to-epub3.xprocspec
modules/scripts/epub2-to-epub3/.install-doc : \
	modules/scripts/epub2-to-epub3/doc/index.md
.make/mk/modules/scripts/epub2-to-epub3/sources.mk : \
	modules/scripts/epub2-to-epub3/src \
	modules/scripts/epub2-to-epub3/src/test \
	modules/scripts/epub2-to-epub3/src/test/resources \
	modules/scripts/epub2-to-epub3/src/test/resources/minimal.epub \
	modules/scripts/epub2-to-epub3/src/test/resources/minimal.epub/META-INF \
	modules/scripts/epub2-to-epub3/src/test/resources/minimal.epub/EPUB \
	modules/scripts/epub2-to-epub3/src/test/java \
	modules/scripts/epub2-to-epub3/src/test/xprocspec \
	modules/scripts/epub2-to-epub3/src/main \
	modules/scripts/epub2-to-epub3/src/main/resources \
	modules/scripts/epub2-to-epub3/src/main/resources/META-INF \
	modules/scripts/epub2-to-epub3/src/main/resources/xml \
	modules/scripts/epub2-to-epub3/doc
