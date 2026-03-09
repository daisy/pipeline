framework/modules-registry/.test framework/modules-registry/.install framework/modules-registry/.install-doc $(TARGET_DIR)/state/framework/modules-registry/modified-since-release_ : \
	framework/modules-registry/src/main/resources/org/daisy/pipeline/xmlcatalog/resources/catalog.dtd \
	framework/modules-registry/src/main/java/org/daisy/pipeline/modules/impl/ModuleUriResolver.java \
	framework/modules-registry/src/main/java/org/daisy/pipeline/modules/impl/DefaultModuleRegistry.java \
	framework/modules-registry/src/main/java/org/daisy/pipeline/modules/Module.java \
	framework/modules-registry/src/main/java/org/daisy/pipeline/modules/XProcResource.java \
	framework/modules-registry/src/main/java/org/daisy/pipeline/modules/ModuleRegistry.java \
	framework/modules-registry/src/main/java/org/daisy/pipeline/modules/Component.java \
	framework/modules-registry/src/main/java/org/daisy/pipeline/modules/XSLTPackage.java \
	framework/modules-registry/src/main/java/org/daisy/pipeline/modules/RelaxNGResource.java \
	framework/modules-registry/src/main/java/org/daisy/pipeline/modules/UseXSLTPackage.java \
	framework/modules-registry/src/main/java/org/daisy/pipeline/modules/JavaDependency.java \
	framework/modules-registry/src/main/java/org/daisy/pipeline/modules/ResourceLoader.java \
	framework/modules-registry/src/main/java/org/daisy/pipeline/modules/Entity.java \
	framework/modules-registry/src/main/java/org/daisy/pipeline/modules/Dependency.java \
	framework/modules-registry/src/main/java/org/daisy/pipeline/modules/XSLTResource.java \
	framework/modules-registry/src/main/java/org/daisy/pipeline/modules/package-info.java \
	framework/modules-registry/src/main/java/org/daisy/pipeline/modules/ResolutionException.java \
	framework/modules-registry/src/main/java/org/daisy/pipeline/xmlcatalog/impl/StaxXmlCatalogParser.java \
	framework/modules-registry/src/main/java/org/daisy/pipeline/xmlcatalog/impl/XmlCatalogConstants.java \
	framework/modules-registry/src/main/java/org/daisy/pipeline/xmlcatalog/impl/package-info.java \
	framework/modules-registry/src/main/java/org/daisy/pipeline/xmlcatalog/XmlCatalogParser.java \
	framework/modules-registry/src/main/java/org/daisy/pipeline/xmlcatalog/XmlCatalog.java \
	framework/modules-registry/src/main/java/org/daisy/pipeline/xmlcatalog/package-info.java
framework/modules-registry/.test framework/modules-registry/.install-doc : \
	framework/modules-registry/src/test/resources/catalog.xml \
	framework/modules-registry/src/test/resources/module/catalog.xml \
	framework/modules-registry/src/test/resources/module/hello.xml \
	framework/modules-registry/src/test/java/org/daisy/pipeline/modules/ModuleTest.java \
	framework/modules-registry/src/test/java/org/daisy/pipeline/xmlcatalog/impl/XmlCatalogParserTest.java
.make/mk/framework/modules-registry/sources.mk : \
	framework/modules-registry/src \
	framework/modules-registry/src/test \
	framework/modules-registry/src/test/resources \
	framework/modules-registry/src/test/resources/module \
	framework/modules-registry/src/test/java \
	framework/modules-registry/src/test/java/org \
	framework/modules-registry/src/test/java/org/daisy \
	framework/modules-registry/src/test/java/org/daisy/pipeline \
	framework/modules-registry/src/test/java/org/daisy/pipeline/modules \
	framework/modules-registry/src/test/java/org/daisy/pipeline/xmlcatalog \
	framework/modules-registry/src/test/java/org/daisy/pipeline/xmlcatalog/impl \
	framework/modules-registry/src/main \
	framework/modules-registry/src/main/resources \
	framework/modules-registry/src/main/resources/org \
	framework/modules-registry/src/main/resources/org/daisy \
	framework/modules-registry/src/main/resources/org/daisy/pipeline \
	framework/modules-registry/src/main/resources/org/daisy/pipeline/xmlcatalog \
	framework/modules-registry/src/main/resources/org/daisy/pipeline/xmlcatalog/resources \
	framework/modules-registry/src/main/java \
	framework/modules-registry/src/main/java/org \
	framework/modules-registry/src/main/java/org/daisy \
	framework/modules-registry/src/main/java/org/daisy/pipeline \
	framework/modules-registry/src/main/java/org/daisy/pipeline/modules \
	framework/modules-registry/src/main/java/org/daisy/pipeline/modules/impl \
	framework/modules-registry/src/main/java/org/daisy/pipeline/xmlcatalog \
	framework/modules-registry/src/main/java/org/daisy/pipeline/xmlcatalog/impl
