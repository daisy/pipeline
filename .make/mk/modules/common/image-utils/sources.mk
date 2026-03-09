modules/common/image-utils/.test modules/common/image-utils/.install modules/common/image-utils/.install-doc $(TARGET_DIR)/state/modules/common/image-utils/modified-since-release_ : \
	modules/common/image-utils/src/main/resources/META-INF/catalog.xml \
	modules/common/image-utils/src/main/resources/xml/library.xsl \
	modules/common/image-utils/src/main/java/org/daisy/pipeline/image/saxon/impl/ImageDimensions.java
modules/common/image-utils/.test modules/common/image-utils/.install-doc : \
	modules/common/image-utils/src/test/xspec/image-dimensions.xspec \
	modules/common/image-utils/src/test/resources/logback.xml \
	modules/common/image-utils/src/test/java/XSpecTest.java
.make/mk/modules/common/image-utils/sources.mk : \
	modules/common/image-utils/src \
	modules/common/image-utils/src/test \
	modules/common/image-utils/src/test/xspec \
	modules/common/image-utils/src/test/resources \
	modules/common/image-utils/src/test/java \
	modules/common/image-utils/src/main \
	modules/common/image-utils/src/main/resources \
	modules/common/image-utils/src/main/resources/META-INF \
	modules/common/image-utils/src/main/resources/xml \
	modules/common/image-utils/src/main/java \
	modules/common/image-utils/src/main/java/org \
	modules/common/image-utils/src/main/java/org/daisy \
	modules/common/image-utils/src/main/java/org/daisy/pipeline \
	modules/common/image-utils/src/main/java/org/daisy/pipeline/image \
	modules/common/image-utils/src/main/java/org/daisy/pipeline/image/saxon \
	modules/common/image-utils/src/main/java/org/daisy/pipeline/image/saxon/impl
