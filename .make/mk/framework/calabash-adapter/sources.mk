framework/calabash-adapter/.test framework/calabash-adapter/.install framework/calabash-adapter/.install-doc $(TARGET_DIR)/state/framework/calabash-adapter/modified-since-release_ : \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/impl/SerializationUtils.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/impl/slf4jXProcMessageListener.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/impl/CalabashXProcPipeline.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/impl/DynamicXProcConfiguration.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/impl/XProcMessageListenerAggregator.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/impl/XProcRuntimeFactoryImpl.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/impl/CalabashXProcResult.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/impl/CalabashXProcEngine.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/impl/MessageListenerImpl.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/impl/XprocMessageHelper.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/BundledConfigurationFileProvider.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/XMLCalabashInputValue.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/XProcBasedTransformer.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/XProcStepProvider.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/XMLCalabashOutputValue.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/XMLCalabashParameterInputValue.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/CalabashXProcError.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/ConfigurationFileProvider.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/XProcStepRegistry.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/XProcStep.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/XMLCalabashOptionValue.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/XProcRuntimeFactory.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/CalabashExceptionFromXProcError.java \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/package-info.java
framework/calabash-adapter/.test framework/calabash-adapter/.install-doc : \
	framework/calabash-adapter/src/test/resources/logback.xml \
	framework/calabash-adapter/src/test/resources/module/progress-messages.xpl \
	framework/calabash-adapter/src/test/resources/module/cx-eval-error.xpl \
	framework/calabash-adapter/src/test/resources/module/error.xpl \
	framework/calabash-adapter/src/test/resources/module/xproc-warning.xpl \
	framework/calabash-adapter/src/test/resources/module/catch-xproc-error.xpl \
	framework/calabash-adapter/src/test/resources/module/xslt-warning.xpl \
	framework/calabash-adapter/src/test/resources/module/xslt-terminate-error.xpl \
	framework/calabash-adapter/src/test/resources/module/java-function-runtime-error.xpl \
	framework/calabash-adapter/src/test/resources/module/catch-xslt-terminate-error.xpl \
	framework/calabash-adapter/src/test/resources/module/xproc-error.xpl \
	framework/calabash-adapter/src/test/resources/module/java-step-runtime-error.xpl \
	framework/calabash-adapter/src/test/resources/META-INF/catalog.xml \
	framework/calabash-adapter/src/test/resources/step.xpl \
	framework/calabash-adapter/src/test/java/ignore \
	framework/calabash-adapter/src/test/java/FrameworkCoreTest.java \
	framework/calabash-adapter/src/test/java/JavaFunction.java \
	framework/calabash-adapter/src/test/java/FrameworkCoreWithDerbyTest.java \
	framework/calabash-adapter/src/test/java/JavaStep.java \
	framework/calabash-adapter/src/test/java/XProcEngineTest.java
.make/mk/framework/calabash-adapter/sources.mk : \
	framework/calabash-adapter/src \
	framework/calabash-adapter/src/test \
	framework/calabash-adapter/src/test/resources \
	framework/calabash-adapter/src/test/resources/module \
	framework/calabash-adapter/src/test/resources/META-INF \
	framework/calabash-adapter/src/test/java \
	framework/calabash-adapter/src/main \
	framework/calabash-adapter/src/main/java \
	framework/calabash-adapter/src/main/java/org \
	framework/calabash-adapter/src/main/java/org/daisy \
	framework/calabash-adapter/src/main/java/org/daisy/common \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash \
	framework/calabash-adapter/src/main/java/org/daisy/common/xproc/calabash/impl
