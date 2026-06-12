modules/pipeline1-adapter/.test modules/pipeline1-adapter/.install modules/pipeline1-adapter/.install-doc $(TARGET_DIR)/state/modules/pipeline1-adapter/modified-since-release_ : \
	modules/pipeline1-adapter/src/main/resources/maven.properties \
	modules/pipeline1-adapter/src/main/resources/lib/org/daisy/pipeline/20250106/pipeline-20250106.zip \
	modules/pipeline1-adapter/src/main/resources/lib/org/daisy/pipeline/20250106/pipeline-20250106.jar \
	modules/pipeline1-adapter/src/main/resources/lib/org/daisy/org.daisy.util/20250106/org.daisy.util-20250106.jar \
	modules/pipeline1-adapter/src/main/java/org/daisy/pipeline/pipeline1/impl/Pipeline1Script.java \
	modules/pipeline1-adapter/src/main/java/org/daisy/pipeline/pipeline1/impl/Pipeline1ClassLoader.java \
	modules/pipeline1-adapter/src/main/java/org/daisy/pipeline/pipeline1/impl/Pipeline1ScriptProvider.java \
	modules/pipeline1-adapter/src/main/java/org/daisy/pipeline/pipeline1/impl/ThreadLocalEnvironment.java
.make/mk/modules/pipeline1-adapter/sources.mk : \
	modules/pipeline1-adapter/src \
	modules/pipeline1-adapter/src/main \
	modules/pipeline1-adapter/src/main/resources \
	modules/pipeline1-adapter/src/main/resources/lib \
	modules/pipeline1-adapter/src/main/resources/lib/org \
	modules/pipeline1-adapter/src/main/resources/lib/org/daisy \
	modules/pipeline1-adapter/src/main/resources/lib/org/daisy/pipeline \
	modules/pipeline1-adapter/src/main/resources/lib/org/daisy/pipeline/20250106 \
	modules/pipeline1-adapter/src/main/resources/lib/org/daisy/org.daisy.util \
	modules/pipeline1-adapter/src/main/resources/lib/org/daisy/org.daisy.util/20250106 \
	modules/pipeline1-adapter/src/main/java \
	modules/pipeline1-adapter/src/main/java/org \
	modules/pipeline1-adapter/src/main/java/org/daisy \
	modules/pipeline1-adapter/src/main/java/org/daisy/pipeline \
	modules/pipeline1-adapter/src/main/java/org/daisy/pipeline/pipeline1 \
	modules/pipeline1-adapter/src/main/java/org/daisy/pipeline/pipeline1/impl
