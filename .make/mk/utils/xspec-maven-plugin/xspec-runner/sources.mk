utils/xspec-maven-plugin/xspec-runner/.test utils/xspec-maven-plugin/xspec-runner/.install utils/xspec-maven-plugin/xspec-runner/.install-doc $(TARGET_DIR)/state/utils/xspec-maven-plugin/xspec-runner/modified-since-release_ : \
	utils/xspec-maven-plugin/xspec-runner/src/main/resources/xspec/reporter/coverage-report.xsl \
	utils/xspec-maven-plugin/xspec-runner/src/main/resources/xspec/reporter/test-report.css \
	utils/xspec-maven-plugin/xspec-runner/src/main/resources/xspec/reporter/format-utils.xsl \
	utils/xspec-maven-plugin/xspec-runner/src/main/resources/xspec/reporter/format-xspec-report-folding.xsl \
	utils/xspec-maven-plugin/xspec-runner/src/main/resources/xspec/reporter/format-xspec-report.xsl \
	utils/xspec-maven-plugin/xspec-runner/src/main/resources/xspec/.gitrepo \
	utils/xspec-maven-plugin/xspec-runner/src/main/resources/xspec/compiler/generate-query-helper.xsl \
	utils/xspec-maven-plugin/xspec-runner/src/main/resources/xspec/compiler/generate-tests-helper.xsl \
	utils/xspec-maven-plugin/xspec-runner/src/main/resources/xspec/compiler/generate-tests-utils.xsl \
	utils/xspec-maven-plugin/xspec-runner/src/main/resources/xspec/compiler/generate-query-utils.xql \
	utils/xspec-maven-plugin/xspec-runner/src/main/resources/xspec/compiler/generate-xspec-tests.xsl \
	utils/xspec-maven-plugin/xspec-runner/src/main/resources/xspec/compiler/generate-common-tests.xsl \
	utils/xspec-maven-plugin/xspec-runner/src/main/resources/xspec/compiler/generate-query-tests.xsl \
	utils/xspec-maven-plugin/xspec-runner/src/main/resources/xspec-extra/format-junit-report.xsl \
	utils/xspec-maven-plugin/xspec-runner/src/main/resources/xspec-extra/format-xspec-summary.xsl \
	utils/xspec-maven-plugin/xspec-runner/src/main/java/org/daisy/maven/xspec/XSpecRunner.java \
	utils/xspec-maven-plugin/xspec-runner/src/main/java/org/daisy/maven/xspec/XSpecResultBuilder.java \
	utils/xspec-maven-plugin/xspec-runner/src/main/java/org/daisy/maven/xspec/TestResults.java
utils/xspec-maven-plugin/xspec-runner/.test utils/xspec-maven-plugin/xspec-runner/.install-doc : \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real/mocking2/catalog.xml \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real/mocking2/test.xspec \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real/mocking2/mock-functions.xsl \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real/mocking2/transform.xsl \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real/mocking/catalog.xml \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real/mocking/test.xspec \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real/mocking/mock-functions.xsl \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real/mocking/transform.xsl \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real/skipped.xspec \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real/test.xspec \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real/error.xspec \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real/nomocking/test.xspec \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real/nomocking/transform.xsl \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real/load.xsl \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real/failure.xspec \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real/resolve-uri.xspec \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real/transform.xsl \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real/complete.xspec \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/foo.xml \
	utils/xspec-maven-plugin/xspec-runner/src/test/java/org/daisy/maven/xspec/XSpecRunnerTest.java
.make/mk/utils/xspec-maven-plugin/xspec-runner/sources.mk : \
	utils/xspec-maven-plugin/xspec-runner/src \
	utils/xspec-maven-plugin/xspec-runner/src/test \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real/mocking2 \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real/mocking \
	utils/xspec-maven-plugin/xspec-runner/src/test/resources/xspec-real/nomocking \
	utils/xspec-maven-plugin/xspec-runner/src/test/java \
	utils/xspec-maven-plugin/xspec-runner/src/test/java/org \
	utils/xspec-maven-plugin/xspec-runner/src/test/java/org/daisy \
	utils/xspec-maven-plugin/xspec-runner/src/test/java/org/daisy/maven \
	utils/xspec-maven-plugin/xspec-runner/src/test/java/org/daisy/maven/xspec \
	utils/xspec-maven-plugin/xspec-runner/src/main \
	utils/xspec-maven-plugin/xspec-runner/src/main/resources \
	utils/xspec-maven-plugin/xspec-runner/src/main/resources/xspec \
	utils/xspec-maven-plugin/xspec-runner/src/main/resources/xspec/reporter \
	utils/xspec-maven-plugin/xspec-runner/src/main/resources/xspec/compiler \
	utils/xspec-maven-plugin/xspec-runner/src/main/resources/xspec-extra \
	utils/xspec-maven-plugin/xspec-runner/src/main/java \
	utils/xspec-maven-plugin/xspec-runner/src/main/java/org \
	utils/xspec-maven-plugin/xspec-runner/src/main/java/org/daisy \
	utils/xspec-maven-plugin/xspec-runner/src/main/java/org/daisy/maven \
	utils/xspec-maven-plugin/xspec-runner/src/main/java/org/daisy/maven/xspec
