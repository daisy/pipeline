clientlib/java/VERSION := 2.2.11-SNAPSHOT

.SECONDARY : clientlib/java/.install
clientlib/java/.install : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/clientlib-java-httpclient/2.1.3-SNAPSHOT/clientlib-java-httpclient-2.1.3-SNAPSHOT.jar

check : $(TARGET_DIR)/state/clientlib/java/last-tested
.PHONY : $(TARGET_DIR)/state/clientlib/java/last-tested
$(TARGET_DIR)/state/clientlib/java/last-tested : \
	$(TARGET_DIR)/state/clientlib/java/clientlib-java/last-tested \
	$(TARGET_DIR)/state/clientlib/java/clientlib-java-httpclient/last-tested
