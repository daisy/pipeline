framework/VERSION := 1.15.7-SNAPSHOT

.SECONDARY : framework/.install
framework/.install : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-bom/1.15.7-SNAPSHOT/framework-bom-1.15.7-SNAPSHOT.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-parent/1.15.7-SNAPSHOT/framework-parent-1.15.7-SNAPSHOT.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/clientlib-java-jaxb/3.1.1-SNAPSHOT/clientlib-java-jaxb-3.1.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-daisy-pipeline/1.14.8-SNAPSHOT/xproc-engine-daisy-pipeline-1.14.8-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/maven/xproc-engine-daisy-pipeline-logging/1.0.1-SNAPSHOT/xproc-engine-daisy-pipeline-logging-1.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/saxon-adapter/5.8.2-SNAPSHOT/saxon-adapter-5.8.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/calabash-adapter/7.1.1-SNAPSHOT/calabash-adapter-7.1.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1-SNAPSHOT/common-utils-6.6.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.0.1-SNAPSHOT/framework-core-12.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-persistence/2.1.14-SNAPSHOT/framework-persistence-2.1.14-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/logging-appender/2.1.8-SNAPSHOT/logging-appender-2.1.8-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules-registry/5.0.2-SNAPSHOT/modules-registry-5.0.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/persistence-derby/2.0.12-SNAPSHOT/persistence-derby-2.0.12-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/webservice/4.0.1-SNAPSHOT/webservice-4.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/xproc-api/8.1.1-SNAPSHOT/xproc-api-8.1.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/pipeline1-adapter/1.1.3-SNAPSHOT/pipeline1-adapter-1.1.3-SNAPSHOT.jar

check : $(TARGET_DIR)/state/framework/last-tested
.PHONY : $(TARGET_DIR)/state/framework/last-tested
$(TARGET_DIR)/state/framework/last-tested : \
	$(TARGET_DIR)/state/framework/bom/last-tested \
	$(TARGET_DIR)/state/framework/parent/last-tested \
	$(TARGET_DIR)/state/framework/utils/clientlib-java-jaxb/last-tested \
	$(TARGET_DIR)/state/framework/utils/xproc-engine-daisy-pipeline/last-tested \
	$(TARGET_DIR)/state/framework/utils/xproc-engine-daisy-pipeline-logging/last-tested \
	$(TARGET_DIR)/state/framework/saxon-adapter/last-tested \
	$(TARGET_DIR)/state/framework/calabash-adapter/last-tested \
	$(TARGET_DIR)/state/framework/common-utils/last-tested \
	$(TARGET_DIR)/state/framework/framework-core/last-tested \
	$(TARGET_DIR)/state/framework/framework-persistence/last-tested \
	$(TARGET_DIR)/state/framework/logging-appender/last-tested \
	$(TARGET_DIR)/state/framework/modules-registry/last-tested \
	$(TARGET_DIR)/state/framework/persistence-derby/last-tested \
	$(TARGET_DIR)/state/framework/webservice/last-tested \
	$(TARGET_DIR)/state/framework/xproc-api/last-tested \
	$(TARGET_DIR)/state/framework/pipeline1-adapter/last-tested

.SECONDARY : framework/.release
framework/.release : | .maven-init .group-eval

framework/.release : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/com.xmlcalabash/1.1.20-p20-98/com.xmlcalabash-1.1.20-p20-98.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-test-helper/3.0.0/modules-test-helper-3.0.0.jar
