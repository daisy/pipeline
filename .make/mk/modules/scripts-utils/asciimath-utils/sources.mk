modules/scripts-utils/asciimath-utils/.test modules/scripts-utils/asciimath-utils/.install modules/scripts-utils/asciimath-utils/.install-doc $(TARGET_DIR)/state/modules/scripts-utils/asciimath-utils/modified-since-release_ : \
	modules/scripts-utils/asciimath-utils/src/main/patches/rhino.patch \
	modules/scripts-utils/asciimath-utils/src/main/resources/META-INF/catalog.xml \
	modules/scripts-utils/asciimath-utils/src/main/resources/xml/library.xpl \
	modules/scripts-utils/asciimath-utils/src/main/resources/xml/library.xsl \
	modules/scripts-utils/asciimath-utils/src/main/resources/javascript/ASCIIMathML.js \
	modules/scripts-utils/asciimath-utils/src/main/java/org/daisy/pipeline/asciimathml/calabash/impl/ASCIIMathToMathMLProvider.java \
	modules/scripts-utils/asciimath-utils/src/main/java/org/daisy/pipeline/asciimathml/ASCIIMathML.java \
	modules/scripts-utils/asciimath-utils/src/main/java/org/daisy/pipeline/asciimath/saxon/impl/ASCIIMathToSSMLFunctionProvider.java
modules/scripts-utils/asciimath-utils/.test modules/scripts-utils/asciimath-utils/.install-doc : \
	modules/scripts-utils/asciimath-utils/src/test/resources/logback.xml \
	modules/scripts-utils/asciimath-utils/src/test/resources/big_list.txt \
	modules/scripts-utils/asciimath-utils/src/test/java/XProcSpecTest.java \
	modules/scripts-utils/asciimath-utils/src/test/java/org/daisy/pipeline/asciimathml/ASCIIMathMLTest.java \
	modules/scripts-utils/asciimath-utils/src/test/xprocspec/test_asciimath-to-mathml.xprocspec
.make/mk/modules/scripts-utils/asciimath-utils/sources.mk : \
	modules/scripts-utils/asciimath-utils/src \
	modules/scripts-utils/asciimath-utils/src/test \
	modules/scripts-utils/asciimath-utils/src/test/resources \
	modules/scripts-utils/asciimath-utils/src/test/java \
	modules/scripts-utils/asciimath-utils/src/test/java/org \
	modules/scripts-utils/asciimath-utils/src/test/java/org/daisy \
	modules/scripts-utils/asciimath-utils/src/test/java/org/daisy/pipeline \
	modules/scripts-utils/asciimath-utils/src/test/java/org/daisy/pipeline/asciimathml \
	modules/scripts-utils/asciimath-utils/src/test/xprocspec \
	modules/scripts-utils/asciimath-utils/src/main \
	modules/scripts-utils/asciimath-utils/src/main/patches \
	modules/scripts-utils/asciimath-utils/src/main/resources \
	modules/scripts-utils/asciimath-utils/src/main/resources/META-INF \
	modules/scripts-utils/asciimath-utils/src/main/resources/xml \
	modules/scripts-utils/asciimath-utils/src/main/resources/javascript \
	modules/scripts-utils/asciimath-utils/src/main/java \
	modules/scripts-utils/asciimath-utils/src/main/java/org \
	modules/scripts-utils/asciimath-utils/src/main/java/org/daisy \
	modules/scripts-utils/asciimath-utils/src/main/java/org/daisy/pipeline \
	modules/scripts-utils/asciimath-utils/src/main/java/org/daisy/pipeline/asciimathml \
	modules/scripts-utils/asciimath-utils/src/main/java/org/daisy/pipeline/asciimathml/calabash \
	modules/scripts-utils/asciimath-utils/src/main/java/org/daisy/pipeline/asciimathml/calabash/impl \
	modules/scripts-utils/asciimath-utils/src/main/java/org/daisy/pipeline/asciimath \
	modules/scripts-utils/asciimath-utils/src/main/java/org/daisy/pipeline/asciimath/saxon \
	modules/scripts-utils/asciimath-utils/src/main/java/org/daisy/pipeline/asciimath/saxon/impl
