utils/xproc-maven-plugin/VERSION := 1.2-SNAPSHOT

.SECONDARY : utils/xproc-maven-plugin/.install
utils/xproc-maven-plugin/.install : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-api/1.3.1-SNAPSHOT/xproc-engine-api-1.3.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-calabash/1.2.1-SNAPSHOT/xproc-engine-calabash-1.2.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xprocspec-runner/1.2.9-SNAPSHOT/xprocspec-runner-1.2.9-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-maven-plugin/1.0.4-SNAPSHOT/xproc-maven-plugin-1.0.4-SNAPSHOT.jar

check : $(TARGET_DIR)/state/utils/xproc-maven-plugin/last-tested
.PHONY : $(TARGET_DIR)/state/utils/xproc-maven-plugin/last-tested
$(TARGET_DIR)/state/utils/xproc-maven-plugin/last-tested : \
	$(TARGET_DIR)/state/utils/xproc-maven-plugin/xproc-engine-api/last-tested \
	$(TARGET_DIR)/state/utils/xproc-maven-plugin/xproc-engine-calabash/last-tested \
	$(TARGET_DIR)/state/utils/xproc-maven-plugin/xprocspec-runner/last-tested \
	$(TARGET_DIR)/state/utils/xproc-maven-plugin/xproc-maven-plugin/last-tested
