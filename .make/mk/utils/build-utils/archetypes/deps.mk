utils/build-utils/archetypes/VERSION := 1.0.0-SNAPSHOT

.SECONDARY : utils/build-utils/archetypes/.install
utils/build-utils/archetypes/.install : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/archetypes/basic-module/1.0.0-SNAPSHOT/basic-module-1.0.0-SNAPSHOT.maven-archetype

check : $(TARGET_DIR)/state/utils/build-utils/archetypes/last-tested
.PHONY : $(TARGET_DIR)/state/utils/build-utils/archetypes/last-tested
$(TARGET_DIR)/state/utils/build-utils/archetypes/last-tested : \
	$(TARGET_DIR)/state/utils/build-utils/archetypes/basic-module/last-tested
