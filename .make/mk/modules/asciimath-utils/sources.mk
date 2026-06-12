modules/asciimath-utils/.test modules/asciimath-utils/.install modules/asciimath-utils/.install-doc $(TARGET_DIR)/state/modules/asciimath-utils/modified-since-release_ : \
	modules/asciimath-utils/src/main/patches/rhino.patch \
	modules/asciimath-utils/src/main/resources/META-INF/catalog.xml \
	modules/asciimath-utils/src/main/resources/xml/library.xpl \
	modules/asciimath-utils/src/main/resources/xml/library.xsl \
	modules/asciimath-utils/src/main/resources/javascript/ASCIIMathML.js \
	modules/asciimath-utils/src/main/java/org/daisy/pipeline/asciimathml/calabash/impl/ASCIIMathToMathMLProvider.java \
	modules/asciimath-utils/src/main/java/org/daisy/pipeline/asciimathml/ASCIIMathML.java \
	modules/asciimath-utils/src/main/java/org/daisy/pipeline/asciimath/saxon/impl/ASCIIMathToSSMLFunctionProvider.java
modules/asciimath-utils/.test modules/asciimath-utils/.install-doc : \
	modules/asciimath-utils/src/test/resources/logback.xml \
	modules/asciimath-utils/src/test/resources/big_list.txt \
	modules/asciimath-utils/src/test/java/XProcSpecTest.java \
	modules/asciimath-utils/src/test/java/org/daisy/pipeline/asciimathml/ASCIIMathMLTest.java \
	modules/asciimath-utils/src/test/xprocspec/test_asciimath-to-mathml.xprocspec
.make/mk/modules/asciimath-utils/sources.mk : \
	modules/asciimath-utils/src \
	modules/asciimath-utils/src/test \
	modules/asciimath-utils/src/test/resources \
	modules/asciimath-utils/src/test/java \
	modules/asciimath-utils/src/test/java/org \
	modules/asciimath-utils/src/test/java/org/daisy \
	modules/asciimath-utils/src/test/java/org/daisy/pipeline \
	modules/asciimath-utils/src/test/java/org/daisy/pipeline/asciimathml \
	modules/asciimath-utils/src/test/xprocspec \
	modules/asciimath-utils/src/main \
	modules/asciimath-utils/src/main/patches \
	modules/asciimath-utils/src/main/resources \
	modules/asciimath-utils/src/main/resources/META-INF \
	modules/asciimath-utils/src/main/resources/xml \
	modules/asciimath-utils/src/main/resources/javascript \
	modules/asciimath-utils/src/main/java \
	modules/asciimath-utils/src/main/java/org \
	modules/asciimath-utils/src/main/java/org/daisy \
	modules/asciimath-utils/src/main/java/org/daisy/pipeline \
	modules/asciimath-utils/src/main/java/org/daisy/pipeline/asciimathml \
	modules/asciimath-utils/src/main/java/org/daisy/pipeline/asciimathml/calabash \
	modules/asciimath-utils/src/main/java/org/daisy/pipeline/asciimathml/calabash/impl \
	modules/asciimath-utils/src/main/java/org/daisy/pipeline/asciimath \
	modules/asciimath-utils/src/main/java/org/daisy/pipeline/asciimath/saxon \
	modules/asciimath-utils/src/main/java/org/daisy/pipeline/asciimath/saxon/impl
