modules/image-utils/.test modules/image-utils/.install modules/image-utils/.install-doc $(TARGET_DIR)/state/modules/image-utils/modified-since-release_ : \
	modules/image-utils/src/main/resources/META-INF/catalog.xml \
	modules/image-utils/src/main/resources/xml/library.xsl \
	modules/image-utils/src/main/java/org/daisy/pipeline/image/saxon/impl/ImageDimensions.java
modules/image-utils/.test modules/image-utils/.install-doc : \
	modules/image-utils/src/test/xspec/image-dimensions.xspec \
	modules/image-utils/src/test/resources/logback.xml \
	modules/image-utils/src/test/java/XSpecTest.java
.make/mk/modules/image-utils/sources.mk : \
	modules/image-utils/src \
	modules/image-utils/src/test \
	modules/image-utils/src/test/xspec \
	modules/image-utils/src/test/resources \
	modules/image-utils/src/test/java \
	modules/image-utils/src/main \
	modules/image-utils/src/main/resources \
	modules/image-utils/src/main/resources/META-INF \
	modules/image-utils/src/main/resources/xml \
	modules/image-utils/src/main/java \
	modules/image-utils/src/main/java/org \
	modules/image-utils/src/main/java/org/daisy \
	modules/image-utils/src/main/java/org/daisy/pipeline \
	modules/image-utils/src/main/java/org/daisy/pipeline/image \
	modules/image-utils/src/main/java/org/daisy/pipeline/image/saxon \
	modules/image-utils/src/main/java/org/daisy/pipeline/image/saxon/impl
