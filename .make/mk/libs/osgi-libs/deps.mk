libs/osgi-libs/VERSION := 1.0-SNAPSHOT

.SECONDARY : libs/osgi-libs/.install
libs/osgi-libs/.install : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/jing/20151127.0.2-SNAPSHOT/jing-20151127.0.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/jnaerator/0.11-p2-SNAPSHOT/jnaerator-0.11-p2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/parboiled/1.0.1-SNAPSHOT/parboiled-1.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/pegdown/1.0.1-SNAPSHOT/pegdown-1.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/saxon-he/10.5-p1-SNAPSHOT/saxon-he-10.5-p1-SNAPSHOT.jar

check : $(TARGET_DIR)/state/libs/osgi-libs/last-tested
.PHONY : $(TARGET_DIR)/state/libs/osgi-libs/last-tested
$(TARGET_DIR)/state/libs/osgi-libs/last-tested : \
	$(TARGET_DIR)/state/libs/osgi-libs/jing/last-tested \
	$(TARGET_DIR)/state/libs/osgi-libs/jnaerator/last-tested \
	$(TARGET_DIR)/state/libs/osgi-libs/parboiled/last-tested \
	$(TARGET_DIR)/state/libs/osgi-libs/pegdown/last-tested \
	$(TARGET_DIR)/state/libs/osgi-libs/saxon/last-tested \
	$(TARGET_DIR)/state/libs/osgi-libs/servlet-api/last-tested
