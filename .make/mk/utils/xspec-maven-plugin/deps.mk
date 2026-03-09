utils/xspec-maven-plugin/VERSION := 1.0.1-SNAPSHOT

.SECONDARY : utils/xspec-maven-plugin/.install
utils/xspec-maven-plugin/.install : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xspec-runner/1.0.6-SNAPSHOT/xspec-runner-1.0.6-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xspec-maven-plugin/1.0.2-SNAPSHOT/xspec-maven-plugin-1.0.2-SNAPSHOT.jar

check : $(TARGET_DIR)/state/utils/xspec-maven-plugin/last-tested
.PHONY : $(TARGET_DIR)/state/utils/xspec-maven-plugin/last-tested
$(TARGET_DIR)/state/utils/xspec-maven-plugin/last-tested : \
	$(TARGET_DIR)/state/utils/xspec-maven-plugin/xspec-runner/last-tested \
	$(TARGET_DIR)/state/utils/xspec-maven-plugin/xspec-maven-plugin/last-tested
