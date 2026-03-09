framework/saxon-adapter/.test framework/saxon-adapter/.install framework/saxon-adapter/.install-doc $(TARGET_DIR)/state/framework/saxon-adapter/modified-since-release_ : \
	framework/saxon-adapter/src/main/java/org/daisy/common/xpath/saxon/impl/XPathFactoryImpl.java \
	framework/saxon-adapter/src/main/java/org/daisy/common/xpath/saxon/ExtensionFunctionProvider.java \
	framework/saxon-adapter/src/main/java/org/daisy/common/xpath/saxon/XPathFunctionRegistry.java \
	framework/saxon-adapter/src/main/java/org/daisy/common/xpath/saxon/XsltFunction.java \
	framework/saxon-adapter/src/main/java/org/daisy/common/xpath/saxon/ReflexiveExtensionFunctionProvider.java \
	framework/saxon-adapter/src/main/java/org/daisy/common/saxon/impl/ProcessorImpl.java \
	framework/saxon-adapter/src/main/java/org/daisy/common/saxon/impl/TransformerFactoryImpl.java \
	framework/saxon-adapter/src/main/java/org/daisy/common/saxon/impl/DocumentBuilderImpl.java \
	framework/saxon-adapter/src/main/java/org/daisy/common/saxon/SaxonOutputValue.java \
	framework/saxon-adapter/src/main/java/org/daisy/common/saxon/SaxonBuffer.java \
	framework/saxon-adapter/src/main/java/org/daisy/common/saxon/SaxonConfigurator.java \
	framework/saxon-adapter/src/main/java/org/daisy/common/saxon/SaxonHelper.java \
	framework/saxon-adapter/src/main/java/org/daisy/common/saxon/xslt/CompiledStylesheet.java \
	framework/saxon-adapter/src/main/java/org/daisy/common/saxon/xslt/XslTransformCompiler.java \
	framework/saxon-adapter/src/main/java/org/daisy/common/saxon/xslt/ThreadUnsafeXslTransformer.java \
	framework/saxon-adapter/src/main/java/org/daisy/common/saxon/SaxonInputValue.java
framework/saxon-adapter/.test framework/saxon-adapter/.install-doc : \
	framework/saxon-adapter/src/test/xspec/test_reflexive-extension-function.xsl \
	framework/saxon-adapter/src/test/xspec/test_reflexive-extension-function.xspec \
	framework/saxon-adapter/src/test/java/org/daisy/common/saxon/xslt/XslTransformTest.java \
	framework/saxon-adapter/src/test/java/MyClassProvider.java
.make/mk/framework/saxon-adapter/sources.mk : \
	framework/saxon-adapter/src \
	framework/saxon-adapter/src/test \
	framework/saxon-adapter/src/test/xspec \
	framework/saxon-adapter/src/test/java \
	framework/saxon-adapter/src/test/java/org \
	framework/saxon-adapter/src/test/java/org/daisy \
	framework/saxon-adapter/src/test/java/org/daisy/common \
	framework/saxon-adapter/src/test/java/org/daisy/common/saxon \
	framework/saxon-adapter/src/test/java/org/daisy/common/saxon/xslt \
	framework/saxon-adapter/src/main \
	framework/saxon-adapter/src/main/java \
	framework/saxon-adapter/src/main/java/org \
	framework/saxon-adapter/src/main/java/org/daisy \
	framework/saxon-adapter/src/main/java/org/daisy/common \
	framework/saxon-adapter/src/main/java/org/daisy/common/xpath \
	framework/saxon-adapter/src/main/java/org/daisy/common/xpath/saxon \
	framework/saxon-adapter/src/main/java/org/daisy/common/xpath/saxon/impl \
	framework/saxon-adapter/src/main/java/org/daisy/common/saxon \
	framework/saxon-adapter/src/main/java/org/daisy/common/saxon/impl \
	framework/saxon-adapter/src/main/java/org/daisy/common/saxon/xslt
