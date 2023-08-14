package org.daisy.pipeline.maven.plugin;

import org.apache.maven.plugin.logging.Log;

import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;

public class GenerateModuleClassFunctionProvider extends ReflexiveExtensionFunctionProvider {

	private final Log logger;

	public GenerateModuleClassFunctionProvider(Log logger) {
		super(GenerateModuleClass.class);
		this.logger = logger;
	}

	public class GenerateModuleClass {

		public String apply(String className,
		                    String moduleName,
		                    String moduleVersion,
		                    String moduleTitle) {
			StringBuilder result = new StringBuilder();
			result.append("package org.daisy.pipeline.modules.impl;\n");
			result.append("\n");
			result.append("import org.daisy.pipeline.modules.Module;\n");
			result.append("import org.daisy.pipeline.xmlcatalog.XmlCatalogParser;\n");
			result.append("\n");
			result.append("import org.osgi.service.component.annotations.Activate;\n");
			result.append("import org.osgi.service.component.annotations.Component;\n");
			result.append("import org.osgi.service.component.annotations.Reference;\n");
			result.append("import org.osgi.service.component.annotations.ReferenceCardinality;\n");
			result.append("import org.osgi.service.component.annotations.ReferencePolicy;\n");
			result.append("\n");
			result.append("@Component(\n");
			result.append("    name = \"org.daisy.pipeline.modules.impl." + className + "\",\n");
			result.append("    service = { Module.class }\n");
			result.append(")\n");
			result.append("public class " + className + " extends Module {\n");
			result.append("\n");
			result.append("    private XmlCatalogParser catalogParser;");
			result.append("\n");
			result.append("    public " + className + "() {\n");
			result.append("        super(\"" + moduleName + "\",\n");
			result.append("              \"" + moduleVersion + "\",\n");
			result.append("              \"" + moduleTitle.replace("\"", "\\\"") + "\");\n");
			result.append("    }\n");
			result.append("\n");
			result.append("    @Activate\n");
			result.append("    public void activate() {\n");
			result.append("        super.init(catalogParser);\n");
			result.append("    }\n");
			result.append("\n");
			result.append("    @Reference(\n");
			result.append("        name = \"XmlCatalogParser\",\n");
			result.append("        unbind = \"-\",\n");
			result.append("        service = XmlCatalogParser.class,\n");
			result.append("        cardinality = ReferenceCardinality.MANDATORY,\n");
			result.append("        policy = ReferencePolicy.STATIC\n");
			result.append("    )\n");
			result.append("    public void setParser(XmlCatalogParser parser) {\n");
			result.append("        catalogParser = parser;\n");
			result.append("    }\n");
			result.append("}\n");
			logger.debug("Successfully generated class " + className);
			return result.toString();
		}
	}
}
