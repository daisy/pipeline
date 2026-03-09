utils/xproc-maven-plugin/xproc-maven-plugin/.test utils/xproc-maven-plugin/xproc-maven-plugin/.install utils/xproc-maven-plugin/xproc-maven-plugin/.install-doc $(TARGET_DIR)/state/utils/xproc-maven-plugin/xproc-maven-plugin/modified-since-release_ : \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/main/java/org/daisy/maven/xproc/plugin/XProcSpecMojo.java \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/main/java/org/daisy/maven/xproc/plugin/XProcMojo.java
utils/xproc-maven-plugin/xproc-maven-plugin/.test utils/xproc-maven-plugin/xproc-maven-plugin/.install-doc : \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-hello-world-calabash/pom.xml \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-hello-world-calabash/src/test/resources/logback.xml \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-hello-world-calabash/src/test/xprocspec/test_identity.xprocspec \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-hello-world-calabash/src/main/xproc/identity.xpl \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/settings.xml \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-catalog-calabash/pom.xml \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-catalog-calabash/src/test/xprocspec/identity.xpl \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-catalog-calabash/src/test/xprocspec/catalog.xml \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-catalog-calabash/src/test/xprocspec/identity-library.xpl \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-catalog-calabash/src/test/xprocspec/1.xprocspec \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-catalog-calabash/src/test/xprocspec/2.xprocspec \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-catalog-calabash/src/test/xprocspec/3.xprocspec \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-error-calabash/pom.xml \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-error-calabash/src/test/xprocspec/test_throw_error.xprocspec \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-error-calabash/src/main/xproc/throw_error.xpl \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-config-calabash/pom.xml \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-config-calabash/src/test/resources/calabash.xml \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-config-calabash/src/test/xprocspec/test_foo.xprocspec \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-config-calabash/src/main/xproc/library.xpl \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-config-calabash/src/main/java/Foo.java
.make/mk/utils/xproc-maven-plugin/xproc-maven-plugin/sources.mk : \
	utils/xproc-maven-plugin/xproc-maven-plugin/src \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-hello-world-calabash \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-hello-world-calabash/src \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-hello-world-calabash/src/test \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-hello-world-calabash/src/test/resources \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-hello-world-calabash/src/test/xprocspec \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-hello-world-calabash/src/main \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-hello-world-calabash/src/main/xproc \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-catalog-calabash \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-catalog-calabash/src \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-catalog-calabash/src/test \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-catalog-calabash/src/test/xprocspec \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-error-calabash \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-error-calabash/src \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-error-calabash/src/test \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-error-calabash/src/test/xprocspec \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-error-calabash/src/main \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-error-calabash/src/main/xproc \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-config-calabash \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-config-calabash/src \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-config-calabash/src/test \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-config-calabash/src/test/resources \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-config-calabash/src/test/xprocspec \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-config-calabash/src/main \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-config-calabash/src/main/xproc \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/it/it-xprocspec-config-calabash/src/main/java \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/main \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/main/java \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/main/java/org \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/main/java/org/daisy \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/main/java/org/daisy/maven \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/main/java/org/daisy/maven/xproc \
	utils/xproc-maven-plugin/xproc-maven-plugin/src/main/java/org/daisy/maven/xproc/plugin
