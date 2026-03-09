modules/scripts-utils/ocr-utils/.test modules/scripts-utils/ocr-utils/.install modules/scripts-utils/ocr-utils/.install-doc $(TARGET_DIR)/state/modules/scripts-utils/ocr-utils/modified-since-release_ : \
	modules/scripts-utils/ocr-utils/src/main/resources/xml/mistral/markdown-to-html.xpl \
	modules/scripts-utils/ocr-utils/src/main/resources/xml/mistral/post-process.xsl \
	modules/scripts-utils/ocr-utils/src/main/resources/maven.properties \
	modules/scripts-utils/ocr-utils/src/main/java/org/daisy/pipeline/ocr/impl/PDFToWordScript.java \
	modules/scripts-utils/ocr-utils/src/main/java/org/daisy/pipeline/ocr/impl/PDFToWordScriptProvider.java \
	modules/scripts-utils/ocr-utils/src/main/java/org/daisy/pipeline/ocr/OCRProcessor.java \
	modules/scripts-utils/ocr-utils/src/main/java/org/daisy/pipeline/ocr/OCRService.java \
	modules/scripts-utils/ocr-utils/src/main/java/org/daisy/pipeline/ocr/mistral/impl/MistralOCRService.java
modules/scripts-utils/ocr-utils/.test modules/scripts-utils/ocr-utils/.install-doc : \
	modules/scripts-utils/ocr-utils/src/test/resources/logback.xml
.make/mk/modules/scripts-utils/ocr-utils/sources.mk : \
	modules/scripts-utils/ocr-utils/src \
	modules/scripts-utils/ocr-utils/src/test \
	modules/scripts-utils/ocr-utils/src/test/resources \
	modules/scripts-utils/ocr-utils/src/main \
	modules/scripts-utils/ocr-utils/src/main/resources \
	modules/scripts-utils/ocr-utils/src/main/resources/xml \
	modules/scripts-utils/ocr-utils/src/main/resources/xml/mistral \
	modules/scripts-utils/ocr-utils/src/main/java \
	modules/scripts-utils/ocr-utils/src/main/java/org \
	modules/scripts-utils/ocr-utils/src/main/java/org/daisy \
	modules/scripts-utils/ocr-utils/src/main/java/org/daisy/pipeline \
	modules/scripts-utils/ocr-utils/src/main/java/org/daisy/pipeline/ocr \
	modules/scripts-utils/ocr-utils/src/main/java/org/daisy/pipeline/ocr/impl \
	modules/scripts-utils/ocr-utils/src/main/java/org/daisy/pipeline/ocr/mistral \
	modules/scripts-utils/ocr-utils/src/main/java/org/daisy/pipeline/ocr/mistral/impl
