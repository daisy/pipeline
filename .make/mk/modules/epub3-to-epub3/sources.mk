modules/epub3-to-epub3/.test modules/epub3-to-epub3/.install modules/epub3-to-epub3/.install-doc $(TARGET_DIR)/state/modules/epub3-to-epub3/modified-since-release_ : \
	modules/epub3-to-epub3/src/main/resources/css/default.css \
	modules/epub3-to-epub3/src/main/resources/META-INF/catalog.xml \
	modules/epub3-to-epub3/src/main/resources/xml/epub3-to-epub3.xpl \
	modules/epub3-to-epub3/src/main/resources/xml/html-derive-meta-from-package-doc.xsl \
	modules/epub3-to-epub3/src/main/resources/xml/braille-rendition.package-document.xsl \
	modules/epub3-to-epub3/src/main/resources/xml/ensure-pagenum-text.xsl \
	modules/epub3-to-epub3/src/main/resources/xml/braille-rendition.fileset.xsl \
	modules/epub3-to-epub3/src/main/resources/xml/resource-map.xsl \
	modules/epub3-to-epub3/src/main/resources/xml/epub3-to-epub3.convert.xpl \
	modules/epub3-to-epub3/src/main/resources/xml/library.xpl
modules/epub3-to-epub3/.test modules/epub3-to-epub3/.install-doc : \
	modules/epub3-to-epub3/src/test/resources/logback.xml \
	modules/epub3-to-epub3/src/test/resources/webserver-tts-config.xml \
	modules/epub3-to-epub3/src/test/resources/valentin.epub \
	modules/epub3-to-epub3/src/test/resources/valentin/META-INF/container.xml \
	modules/epub3-to-epub3/src/test/resources/valentin/mimetype \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/C00000-4-chapter.xhtml \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/nav.xhtml \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/C00000-7-chapter.xhtml \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/C00000-3-chapter.xhtml \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/css/accessibility.css \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/css/fonts/opendyslexic/OpenDyslexicMono-Regular.otf \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/css/fonts/opendyslexic/OpenDyslexic-Italic.otf \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/css/fonts/opendyslexic/OpenDyslexic-BoldItalic.otf \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/css/fonts/opendyslexic/OpenDyslexic-Bold.otf \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/css/fonts/opendyslexic/LICENSE.txt \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/css/fonts/opendyslexic/OpenDyslexic-Regular.otf \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/images/valentin.jpg \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/C00000-1-cover.xhtml \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/C00000-2-frontmatter.xhtml \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/C00000-6-rearnotes.xhtml \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/nav.ncx \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/C00000-9-footnotes.xhtml \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/C00000-8-conclusion.xhtml \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/C00000-5-chapter.xhtml \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/package.opf \
	modules/epub3-to-epub3/src/test/java/XProcSpecTest.java \
	modules/epub3-to-epub3/src/test/xprocspec/test_epub3-to-epub3.xprocspec \
	modules/epub3-to-epub3/src/test/xprocspec/test_epub3-to-epub3.load.xprocspec \
	modules/epub3-to-epub3/src/test/xprocspec/test_epub3-to-epub3.script.xprocspec
modules/epub3-to-epub3/.install-doc : \
	modules/epub3-to-epub3/doc/index.md
.make/mk/modules/epub3-to-epub3/sources.mk : \
	modules/epub3-to-epub3/src \
	modules/epub3-to-epub3/src/test \
	modules/epub3-to-epub3/src/test/resources \
	modules/epub3-to-epub3/src/test/resources/valentin \
	modules/epub3-to-epub3/src/test/resources/valentin/META-INF \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/css \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/css/fonts \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/css/fonts/opendyslexic \
	modules/epub3-to-epub3/src/test/resources/valentin/EPUB/images \
	modules/epub3-to-epub3/src/test/java \
	modules/epub3-to-epub3/src/test/xprocspec \
	modules/epub3-to-epub3/src/main \
	modules/epub3-to-epub3/src/main/resources \
	modules/epub3-to-epub3/src/main/resources/css \
	modules/epub3-to-epub3/src/main/resources/META-INF \
	modules/epub3-to-epub3/src/main/resources/xml \
	modules/epub3-to-epub3/doc
