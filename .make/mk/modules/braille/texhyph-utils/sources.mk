modules/braille/texhyph-utils/.test modules/braille/texhyph-utils/.install modules/braille/texhyph-utils/.install-doc $(TARGET_DIR)/state/modules/braille/texhyph-utils/modified-since-release_ : \
	modules/braille/texhyph-utils/src/main/resources/xml/library.xsl \
	modules/braille/texhyph-utils/src/main/java/org/daisy/pipeline/braille/tex/impl/TexHyphenatorDotifyImpl.java \
	modules/braille/texhyph-utils/src/main/java/org/daisy/pipeline/braille/tex/impl/TexHyphenatorSimpleImpl.java \
	modules/braille/texhyph-utils/src/main/java/org/daisy/pipeline/braille/tex/impl/TexHyphenatorTableRegistry.java \
	modules/braille/texhyph-utils/src/main/java/org/daisy/pipeline/braille/tex/TexHyphenator.java \
	modules/braille/texhyph-utils/src/main/java/org/daisy/pipeline/braille/tex/TexHyphenatorTablePath.java \
	modules/braille/texhyph-utils/src/main/java/org/daisy/pipeline/braille/tex/package-info.java \
	modules/braille/texhyph-utils/src/main/README.md
modules/braille/texhyph-utils/.test modules/braille/texhyph-utils/.install-doc : \
	modules/braille/texhyph-utils/src/test/xspec/.gitkeep \
	modules/braille/texhyph-utils/src/test/resources/logback.xml \
	modules/braille/texhyph-utils/src/test/resources/tables/foobar.properties \
	modules/braille/texhyph-utils/src/test/resources/tables/foobar.tex \
	modules/braille/texhyph-utils/src/test/resources/OSGI-INF/table-path.xml \
	modules/braille/texhyph-utils/src/test/resources/phony.xsl \
	modules/braille/texhyph-utils/src/test/resources/foobar.tex \
	modules/braille/texhyph-utils/src/test/java/TexHyphenatorCoreTest.java \
	modules/braille/texhyph-utils/src/test/java/TablePath.java \
	modules/braille/texhyph-utils/src/test/java/XSpecTest.java
modules/braille/texhyph-utils/.install-doc : \
	modules/braille/texhyph-utils/doc/index.md
.make/mk/modules/braille/texhyph-utils/sources.mk : \
	modules/braille/texhyph-utils/src \
	modules/braille/texhyph-utils/src/test \
	modules/braille/texhyph-utils/src/test/xspec \
	modules/braille/texhyph-utils/src/test/resources \
	modules/braille/texhyph-utils/src/test/resources/tables \
	modules/braille/texhyph-utils/src/test/resources/OSGI-INF \
	modules/braille/texhyph-utils/src/test/java \
	modules/braille/texhyph-utils/src/main \
	modules/braille/texhyph-utils/src/main/resources \
	modules/braille/texhyph-utils/src/main/resources/xml \
	modules/braille/texhyph-utils/src/main/java \
	modules/braille/texhyph-utils/src/main/java/org \
	modules/braille/texhyph-utils/src/main/java/org/daisy \
	modules/braille/texhyph-utils/src/main/java/org/daisy/pipeline \
	modules/braille/texhyph-utils/src/main/java/org/daisy/pipeline/braille \
	modules/braille/texhyph-utils/src/main/java/org/daisy/pipeline/braille/tex \
	modules/braille/texhyph-utils/src/main/java/org/daisy/pipeline/braille/tex/impl \
	modules/braille/texhyph-utils/doc
