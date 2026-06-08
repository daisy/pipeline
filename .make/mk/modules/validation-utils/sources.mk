modules/validation-utils/.test modules/validation-utils/.install modules/validation-utils/.install-doc $(TARGET_DIR)/state/modules/validation-utils/modified-since-release_ : \
	modules/validation-utils/src/main/resources/META-INF/catalog.xml \
	modules/validation-utils/src/main/resources/xml/xproc/check-files-wellformed.xpl \
	modules/validation-utils/src/main/resources/xml/xproc/create-validation-report-error.xpl \
	modules/validation-utils/src/main/resources/xml/xproc/report-errors.xpl \
	modules/validation-utils/src/main/resources/xml/xproc/combine-validation-reports.xpl \
	modules/validation-utils/src/main/resources/xml/xproc/validation-utils-library.xpl \
	modules/validation-utils/src/main/resources/xml/xproc/relax-ng-report.xpl \
	modules/validation-utils/src/main/resources/xml/xproc/relax-ng-to-schematron.xpl \
	modules/validation-utils/src/main/resources/xml/xproc/check-files-exist.xpl \
	modules/validation-utils/src/main/resources/xml/xproc/validation-report-to-html.xpl \
	modules/validation-utils/src/main/resources/xml/xproc/validation-status.xpl \
	modules/validation-utils/src/main/resources/xml/xproc/create-validation-report-error-for-file.xpl \
	modules/validation-utils/src/main/resources/xml/schema/document-validation-report.rng \
	modules/validation-utils/src/main/resources/xml/xslt/cerrors-to-derrors.xsl \
	modules/validation-utils/src/main/resources/xml/xslt/relaxng2isoschematron.xsl \
	modules/validation-utils/src/main/resources/xml/xslt/validation-report-to-html.xsl
modules/validation-utils/.test modules/validation-utils/.install-doc : \
	modules/validation-utils/src/test/sample-output-from-combine-validation-reports2.xml \
	modules/validation-utils/src/test/sample-fileset.xml \
	modules/validation-utils/src/test/badxml.xml \
	modules/validation-utils/src/test/xprocspec/test_validate-with-relax-ng.xprocspec \
	modules/validation-utils/src/test/sample-schematron-report.xml \
	modules/validation-utils/src/test/sample-rng-report.xml \
	modules/validation-utils/src/test/sample-images-report.xml \
	modules/validation-utils/src/test/sample-output-from-validation-report-to-html.html \
	modules/validation-utils/src/test/sample-output-from-combine-validation-reports.xml
.make/mk/modules/validation-utils/sources.mk : \
	modules/validation-utils/src \
	modules/validation-utils/src/test \
	modules/validation-utils/src/test/xprocspec \
	modules/validation-utils/src/main \
	modules/validation-utils/src/main/resources \
	modules/validation-utils/src/main/resources/META-INF \
	modules/validation-utils/src/main/resources/xml \
	modules/validation-utils/src/main/resources/xml/xproc \
	modules/validation-utils/src/main/resources/xml/schema \
	modules/validation-utils/src/main/resources/xml/xslt
