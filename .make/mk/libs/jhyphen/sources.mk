libs/jhyphen/.test libs/jhyphen/.install libs/jhyphen/.install-doc $(TARGET_DIR)/state/libs/jhyphen/modified-since-release_ : \
	libs/jhyphen/src/main/java/ch/sbs/jhyphen/Hyphen.java \
	libs/jhyphen/src/main/java/ch/sbs/jhyphen/HyphenationException.java \
	libs/jhyphen/src/main/java/ch/sbs/jhyphen/CompilationException.java \
	libs/jhyphen/src/main/java/ch/sbs/jhyphen/Hyphenator.java \
	libs/jhyphen/src/main/java/ch/sbs/jhyphen/StandardHyphenationException.java
libs/jhyphen/.test libs/jhyphen/.install-doc : \
	libs/jhyphen/src/test/resources/tables/standard.dic \
	libs/jhyphen/src/test/resources/tables/invalid_charset.dic \
	libs/jhyphen/src/test/resources/tables/invalid.dic \
	libs/jhyphen/src/test/resources/tables/non-standard.dic \
	libs/jhyphen/src/test/resources/test.txt \
	libs/jhyphen/src/test/java/ch/sbs/jhyphen/HyphenatorTest.java
.make/mk/libs/jhyphen/sources.mk : \
	libs/jhyphen/src \
	libs/jhyphen/src/test \
	libs/jhyphen/src/test/resources \
	libs/jhyphen/src/test/resources/tables \
	libs/jhyphen/src/test/java \
	libs/jhyphen/src/test/java/ch \
	libs/jhyphen/src/test/java/ch/sbs \
	libs/jhyphen/src/test/java/ch/sbs/jhyphen \
	libs/jhyphen/src/main \
	libs/jhyphen/src/main/java \
	libs/jhyphen/src/main/java/ch \
	libs/jhyphen/src/main/java/ch/sbs \
	libs/jhyphen/src/main/java/ch/sbs/jhyphen
