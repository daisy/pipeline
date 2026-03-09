utils/build-utils/VERSION := 1.0-SNAPSHOT

.SECONDARY : utils/build-utils/.install
utils/build-utils/.install : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-test-helper/2.2.7-SNAPSHOT/modules-test-helper-2.2.7-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/ds-to-spi-annotations/1.0.1-SNAPSHOT/ds-to-spi-annotations-1.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/ds-to-spi-annotations-processor/1.1.5-SNAPSHOT/ds-to-spi-annotations-processor-1.1.5-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/ds-to-spi-runtime/1.2.2-SNAPSHOT/ds-to-spi-runtime-1.2.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/ds-to-spi-maven-plugin/1.1.6-SNAPSHOT/ds-to-spi-maven-plugin-1.1.6-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/pax-exam-helper/2.5.2-SNAPSHOT/pax-exam-helper-2.5.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/archetypes/basic-module/1.0.0-SNAPSHOT/basic-module-1.0.0-SNAPSHOT.maven-archetype

check : $(TARGET_DIR)/state/utils/build-utils/last-tested
.PHONY : $(TARGET_DIR)/state/utils/build-utils/last-tested
$(TARGET_DIR)/state/utils/build-utils/last-tested : \
	$(TARGET_DIR)/state/utils/build-utils/modules-build-helper/last-tested \
	$(TARGET_DIR)/state/utils/build-utils/modules-test-helper/last-tested \
	$(TARGET_DIR)/state/utils/build-utils/ds-to-spi/last-tested \
	$(TARGET_DIR)/state/utils/build-utils/pax-exam-helper/last-tested \
	$(TARGET_DIR)/state/utils/build-utils/archetypes/last-tested
