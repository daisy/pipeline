utils/build-utils/ds-to-spi/VERSION := 1.1.2-SNAPSHOT

.SECONDARY : utils/build-utils/ds-to-spi/.install
utils/build-utils/ds-to-spi/.install : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/ds-to-spi-annotations/1.0.1-SNAPSHOT/ds-to-spi-annotations-1.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/ds-to-spi-annotations-processor/1.1.5-SNAPSHOT/ds-to-spi-annotations-processor-1.1.5-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/ds-to-spi-runtime/1.2.2-SNAPSHOT/ds-to-spi-runtime-1.2.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/ds-to-spi-maven-plugin/1.1.6-SNAPSHOT/ds-to-spi-maven-plugin-1.1.6-SNAPSHOT.jar

check : $(TARGET_DIR)/state/utils/build-utils/ds-to-spi/last-tested
.PHONY : $(TARGET_DIR)/state/utils/build-utils/ds-to-spi/last-tested
$(TARGET_DIR)/state/utils/build-utils/ds-to-spi/last-tested : \
	$(TARGET_DIR)/state/utils/build-utils/ds-to-spi/ds-to-spi-annotations/last-tested \
	$(TARGET_DIR)/state/utils/build-utils/ds-to-spi/ds-to-spi-annotations-processor/last-tested \
	$(TARGET_DIR)/state/utils/build-utils/ds-to-spi/ds-to-spi-runtime/last-tested \
	$(TARGET_DIR)/state/utils/build-utils/ds-to-spi/ds-to-spi-maven-plugin/last-tested
