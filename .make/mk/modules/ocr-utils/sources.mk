modules/ocr-utils/.test modules/ocr-utils/.install modules/ocr-utils/.install-doc $(TARGET_DIR)/state/modules/ocr-utils/modified-since-release_ : \
	modules/ocr-utils/src/main/resources/xml/mistral/markdown-to-html.xpl \
	modules/ocr-utils/src/main/resources/xml/mistral/post-process.xsl \
	modules/ocr-utils/src/main/resources/maven.properties \
	modules/ocr-utils/src/main/java/org/daisy/pipeline/ocr/impl/PDFToWordScript.java \
	modules/ocr-utils/src/main/java/org/daisy/pipeline/ocr/impl/PDFToWordScriptProvider.java \
	modules/ocr-utils/src/main/java/org/daisy/pipeline/ocr/OCRProcessor.java \
	modules/ocr-utils/src/main/java/org/daisy/pipeline/ocr/OCRService.java \
	modules/ocr-utils/src/main/java/org/daisy/pipeline/ocr/mistral/impl/MistralOCRService.java
modules/ocr-utils/.test modules/ocr-utils/.install-doc : \
	modules/ocr-utils/src/test/resources/logback.xml
.make/mk/modules/ocr-utils/sources.mk : \
	modules/ocr-utils/src \
	modules/ocr-utils/src/test \
	modules/ocr-utils/src/test/resources \
	modules/ocr-utils/src/main \
	modules/ocr-utils/src/main/resources \
	modules/ocr-utils/src/main/resources/xml \
	modules/ocr-utils/src/main/resources/xml/mistral \
	modules/ocr-utils/src/main/java \
	modules/ocr-utils/src/main/java/org \
	modules/ocr-utils/src/main/java/org/daisy \
	modules/ocr-utils/src/main/java/org/daisy/pipeline \
	modules/ocr-utils/src/main/java/org/daisy/pipeline/ocr \
	modules/ocr-utils/src/main/java/org/daisy/pipeline/ocr/impl \
	modules/ocr-utils/src/main/java/org/daisy/pipeline/ocr/mistral \
	modules/ocr-utils/src/main/java/org/daisy/pipeline/ocr/mistral/impl
