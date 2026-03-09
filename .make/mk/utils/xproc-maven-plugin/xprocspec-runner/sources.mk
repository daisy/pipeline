utils/xproc-maven-plugin/xprocspec-runner/.test utils/xproc-maven-plugin/xprocspec-runner/.install utils/xproc-maven-plugin/xprocspec-runner/.install-doc $(TARGET_DIR)/state/utils/xproc-maven-plugin/xprocspec-runner/modified-since-release_ : \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/resources/xprocspec-extra/xspec.css \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/resources/xprocspec-extra/custom-assertion-steps/compare-exact.xpl \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/resources/xprocspec-extra/custom-assertion-steps/compare-except-ids.xpl \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/resources/xprocspec-extra/custom-assertion-steps/make-prefixes-and-namespaces-explicit.xsl \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/resources/xprocspec-extra/custom-assertion-steps/library.xpl \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/resources/xprocspec-extra/xprocspec-summary.xpl \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/resources/META-INF/catalog.xml \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/java/org/daisy/maven/xproc/xprocspec/XProcSpecRunner.java \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/java/org/daisy/maven/xproc/xprocspec/logging/pipeline/impl/MessageStepProvider.java \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/java/org/daisy/maven/xproc/xprocspec/logging/calabash/impl/MessageStep.java
utils/xproc-maven-plugin/xprocspec-runner/.test utils/xproc-maven-plugin/xprocspec-runner/.install-doc : \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/logback.xml \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/xprocspec/identity.xpl \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/xprocspec/throw_java_error_declaration.xpl \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/xprocspec/throw_error.xpl \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/xprocspec/throw_java_error_implementation_java.xml \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/xprocspec/test_non_existing.xprocspec \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/xprocspec/test_identity_broken.xprocspec \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/xprocspec/test_identity.xprocspec \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/xprocspec/test_throw_error.xprocspec \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/xprocspec/foo_implementation.xpl \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/xprocspec/test_throw_error_unexpected.xprocspec \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/xprocspec/test_throw_java_error.xprocspec \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/xprocspec/test_foo_catalog.xprocspec \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/xprocspec/foo_catalog.xml \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/xprocspec/identity_broken.xpl \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/xprocspec/foo_declaration.xpl \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/xprocspec/foo_implementation_java.xml \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/xprocspec/test_identity_pending.xprocspec \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/xprocspec/test_very_big.xprocspec \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/xprocspec/test_foo_java.xprocspec \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/xprocspec/test_custom_assertion.xprocspec \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/java/org/daisy/maven/xproc/xprocspec/ThrowJavaError.java \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/java/org/daisy/maven/xproc/xprocspec/PerformanceTest.java \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/java/org/daisy/maven/xproc/xprocspec/XProcSpecRunnerTest.java \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/java/org/daisy/maven/xproc/xprocspec/Foo.java
.make/mk/utils/xproc-maven-plugin/xprocspec-runner/sources.mk : \
	utils/xproc-maven-plugin/xprocspec-runner/src \
	utils/xproc-maven-plugin/xprocspec-runner/src/test \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/resources/xprocspec \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/java \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/java/org \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/java/org/daisy \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/java/org/daisy/maven \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/java/org/daisy/maven/xproc \
	utils/xproc-maven-plugin/xprocspec-runner/src/test/java/org/daisy/maven/xproc/xprocspec \
	utils/xproc-maven-plugin/xprocspec-runner/src/main \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/resources \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/resources/xprocspec-extra \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/resources/xprocspec-extra/custom-assertion-steps \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/resources/META-INF \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/java \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/java/org \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/java/org/daisy \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/java/org/daisy/maven \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/java/org/daisy/maven/xproc \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/java/org/daisy/maven/xproc/xprocspec \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/java/org/daisy/maven/xproc/xprocspec/logging \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/java/org/daisy/maven/xproc/xprocspec/logging/pipeline \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/java/org/daisy/maven/xproc/xprocspec/logging/pipeline/impl \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/java/org/daisy/maven/xproc/xprocspec/logging/calabash \
	utils/xproc-maven-plugin/xprocspec-runner/src/main/java/org/daisy/maven/xproc/xprocspec/logging/calabash/impl
