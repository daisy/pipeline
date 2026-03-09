modules/common/validation-utils/.test modules/common/validation-utils/.install modules/common/validation-utils/.install-doc $(TARGET_DIR)/state/modules/common/validation-utils/modified-since-release_ : \
	modules/common/validation-utils/src/main/resources/META-INF/catalog.xml \
	modules/common/validation-utils/src/main/resources/xml/xproc/check-files-wellformed.xpl \
	modules/common/validation-utils/src/main/resources/xml/xproc/create-validation-report-error.xpl \
	modules/common/validation-utils/src/main/resources/xml/xproc/report-errors.xpl \
	modules/common/validation-utils/src/main/resources/xml/xproc/combine-validation-reports.xpl \
	modules/common/validation-utils/src/main/resources/xml/xproc/validation-utils-library.xpl \
	modules/common/validation-utils/src/main/resources/xml/xproc/relax-ng-report.xpl \
	modules/common/validation-utils/src/main/resources/xml/xproc/relax-ng-to-schematron.xpl \
	modules/common/validation-utils/src/main/resources/xml/xproc/check-files-exist.xpl \
	modules/common/validation-utils/src/main/resources/xml/xproc/validation-report-to-html.xpl \
	modules/common/validation-utils/src/main/resources/xml/xproc/validation-status.xpl \
	modules/common/validation-utils/src/main/resources/xml/xproc/create-validation-report-error-for-file.xpl \
	modules/common/validation-utils/src/main/resources/xml/schema/document-validation-report.rng \
	modules/common/validation-utils/src/main/resources/xml/xslt/cerrors-to-derrors.xsl \
	modules/common/validation-utils/src/main/resources/xml/xslt/relaxng2isoschematron.xsl \
	modules/common/validation-utils/src/main/resources/xml/xslt/validation-report-to-html.xsl
modules/common/validation-utils/.test modules/common/validation-utils/.install-doc : \
	modules/common/validation-utils/src/test/sample-output-from-combine-validation-reports2.xml \
	modules/common/validation-utils/src/test/sample-fileset.xml \
	modules/common/validation-utils/src/test/badxml.xml \
	modules/common/validation-utils/src/test/xprocspec/test_validate-with-relax-ng.xprocspec \
	modules/common/validation-utils/src/test/sample-schematron-report.xml \
	modules/common/validation-utils/src/test/sample-rng-report.xml \
	modules/common/validation-utils/src/test/sample-images-report.xml \
	modules/common/validation-utils/src/test/sample-output-from-validation-report-to-html.html \
	modules/common/validation-utils/src/test/sample-output-from-combine-validation-reports.xml
.make/mk/modules/common/validation-utils/sources.mk : \
	modules/common/validation-utils/src \
	modules/common/validation-utils/src/test \
	modules/common/validation-utils/src/test/xprocspec \
	modules/common/validation-utils/src/main \
	modules/common/validation-utils/src/main/resources \
	modules/common/validation-utils/src/main/resources/META-INF \
	modules/common/validation-utils/src/main/resources/xml \
	modules/common/validation-utils/src/main/resources/xml/xproc \
	modules/common/validation-utils/src/main/resources/xml/schema \
	modules/common/validation-utils/src/main/resources/xml/xslt
