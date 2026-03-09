modules/scripts/epub3-to-epub3/.test modules/scripts/epub3-to-epub3/.install modules/scripts/epub3-to-epub3/.install-doc $(TARGET_DIR)/state/modules/scripts/epub3-to-epub3/modified-since-release_ : \
	modules/scripts/epub3-to-epub3/src/main/resources/css/default.css \
	modules/scripts/epub3-to-epub3/src/main/resources/META-INF/catalog.xml \
	modules/scripts/epub3-to-epub3/src/main/resources/xml/epub3-to-epub3.xpl \
	modules/scripts/epub3-to-epub3/src/main/resources/xml/html-derive-meta-from-package-doc.xsl \
	modules/scripts/epub3-to-epub3/src/main/resources/xml/braille-rendition.package-document.xsl \
	modules/scripts/epub3-to-epub3/src/main/resources/xml/ensure-pagenum-text.xsl \
	modules/scripts/epub3-to-epub3/src/main/resources/xml/braille-rendition.fileset.xsl \
	modules/scripts/epub3-to-epub3/src/main/resources/xml/resource-map.xsl \
	modules/scripts/epub3-to-epub3/src/main/resources/xml/epub3-to-epub3.convert.xpl \
	modules/scripts/epub3-to-epub3/src/main/resources/xml/library.xpl
modules/scripts/epub3-to-epub3/.test modules/scripts/epub3-to-epub3/.install-doc : \
	modules/scripts/epub3-to-epub3/src/test/resources/logback.xml \
	modules/scripts/epub3-to-epub3/src/test/resources/webserver-tts-config.xml \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin.epub \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/META-INF/container.xml \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/mimetype \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/C00000-4-chapter.xhtml \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/nav.xhtml \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/C00000-7-chapter.xhtml \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/C00000-3-chapter.xhtml \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/css/accessibility.css \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/css/fonts/opendyslexic/OpenDyslexicMono-Regular.otf \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/css/fonts/opendyslexic/OpenDyslexic-Italic.otf \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/css/fonts/opendyslexic/OpenDyslexic-BoldItalic.otf \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/css/fonts/opendyslexic/OpenDyslexic-Bold.otf \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/css/fonts/opendyslexic/LICENSE.txt \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/css/fonts/opendyslexic/OpenDyslexic-Regular.otf \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/images/valentin.jpg \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/C00000-1-cover.xhtml \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/C00000-2-frontmatter.xhtml \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/C00000-6-rearnotes.xhtml \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/nav.ncx \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/C00000-9-footnotes.xhtml \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/C00000-8-conclusion.xhtml \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/C00000-5-chapter.xhtml \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/package.opf \
	modules/scripts/epub3-to-epub3/src/test/java/XProcSpecTest.java \
	modules/scripts/epub3-to-epub3/src/test/xprocspec/test_epub3-to-epub3.xprocspec \
	modules/scripts/epub3-to-epub3/src/test/xprocspec/test_epub3-to-epub3.load.xprocspec \
	modules/scripts/epub3-to-epub3/src/test/xprocspec/test_epub3-to-epub3.script.xprocspec
modules/scripts/epub3-to-epub3/.install-doc : \
	modules/scripts/epub3-to-epub3/doc/index.md
.make/mk/modules/scripts/epub3-to-epub3/sources.mk : \
	modules/scripts/epub3-to-epub3/src \
	modules/scripts/epub3-to-epub3/src/test \
	modules/scripts/epub3-to-epub3/src/test/resources \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/META-INF \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/css \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/css/fonts \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/css/fonts/opendyslexic \
	modules/scripts/epub3-to-epub3/src/test/resources/valentin/EPUB/images \
	modules/scripts/epub3-to-epub3/src/test/java \
	modules/scripts/epub3-to-epub3/src/test/xprocspec \
	modules/scripts/epub3-to-epub3/src/main \
	modules/scripts/epub3-to-epub3/src/main/resources \
	modules/scripts/epub3-to-epub3/src/main/resources/css \
	modules/scripts/epub3-to-epub3/src/main/resources/META-INF \
	modules/scripts/epub3-to-epub3/src/main/resources/xml \
	modules/scripts/epub3-to-epub3/doc
