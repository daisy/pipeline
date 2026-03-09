utils/xproc-maven-plugin/xproc-engine-calabash/.test utils/xproc-maven-plugin/xproc-engine-calabash/.install utils/xproc-maven-plugin/xproc-engine-calabash/.install-doc $(TARGET_DIR)/state/utils/xproc-maven-plugin/xproc-engine-calabash/modified-since-release_ : \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/main/resources/META-INF/services/org.daisy.maven.xproc.api.XProcEngine \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/main/java/org/daisy/maven/xproc/calabash/Calabash.java
utils/xproc-maven-plugin/xproc-engine-calabash/.test utils/xproc-maven-plugin/xproc-engine-calabash/.install-doc : \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/test/resources/hello_foo_expected.xml \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/test/resources/foo.xpl \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/test/resources/hello.xml \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/test/java/org/daisy/maven/xproc/calabash/CalabashTest.java
.make/mk/utils/xproc-maven-plugin/xproc-engine-calabash/sources.mk : \
	utils/xproc-maven-plugin/xproc-engine-calabash/src \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/test \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/test/resources \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/test/java \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/test/java/org \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/test/java/org/daisy \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/test/java/org/daisy/maven \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/test/java/org/daisy/maven/xproc \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/test/java/org/daisy/maven/xproc/calabash \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/main \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/main/resources \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/main/resources/META-INF \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/main/resources/META-INF/services \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/main/java \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/main/java/org \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/main/java/org/daisy \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/main/java/org/daisy/maven \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/main/java/org/daisy/maven/xproc \
	utils/xproc-maven-plugin/xproc-engine-calabash/src/main/java/org/daisy/maven/xproc/calabash
