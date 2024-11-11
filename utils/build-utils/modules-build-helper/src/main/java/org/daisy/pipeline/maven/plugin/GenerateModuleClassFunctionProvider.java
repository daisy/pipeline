package org.daisy.pipeline.maven.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;

import org.apache.maven.plugin.logging.Log;

import org.daisy.common.spi.ServiceLoader;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;
import org.daisy.pipeline.modules.Component;
import org.daisy.pipeline.modules.Dependency;
import org.daisy.pipeline.modules.JavaDependency;
import org.daisy.pipeline.modules.Module;
import org.daisy.pipeline.modules.ModuleRegistry;
import org.daisy.pipeline.modules.RelaxNGResource;
import org.daisy.pipeline.modules.ResourceLoader;
import org.daisy.pipeline.modules.UseXSLTPackage;
import org.daisy.pipeline.modules.XProcResource;
import org.daisy.pipeline.modules.XSLTPackage;
import org.daisy.pipeline.modules.XSLTResource;

import org.osgi.framework.Version;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GenerateModuleClassFunctionProvider extends ReflexiveExtensionFunctionProvider {

	private final XMLInputFactory xmlParser;
	private final Set<File> sourceRoots;
	private final ModuleRegistry moduleRegistry;
	private final Log logger;

	public GenerateModuleClassFunctionProvider(ClassLoader classPath, Collection<String> sourceRoots, Log logger) {
		super(GenerateModuleClass.class);
		xmlParser = XMLInputFactory.newInstance();
		this.sourceRoots = new HashSet<>(); {
			for (String dir : sourceRoots) {
				File f = new File(dir);
				if (f.exists())
					this.sourceRoots.add(f); }}
		moduleRegistry = getModuleRegistry(classPath);
		this.logger = logger;
	}

	public class GenerateModuleClass {

		public String apply(String className,
		                    String moduleName,
		                    String moduleVersion,
		                    String moduleTitle,
		                    Element catalog) {
			Set<CatalogEntry> catalogEntries = parseCatalog(catalog);
			URI catalogBaseURI = URI.create(catalog.getBaseURI());
			// fake module to pass to {XProc,XSLT,RelaxNG}Resource
			Module moduleUnderCompilation = new Module(moduleName, moduleVersion, moduleTitle, new ResourceLoader() {
					@Override
					public URL loadResource(String path) throws NoSuchFileException {
						URI uri = catalogBaseURI.resolve(path);
						try {
							// catalogBaseURI expected to be a file URI
							if (!new File(uri).exists())
								throw new NoSuchFileException("file does not exist: " + path);
						} catch (IllegalArgumentException e) {
							throw new NoSuchFileException("file does not exist: " + path);
						}
						try {
							return uri.toURL();
						} catch (MalformedURLException e) {
							throw new IllegalStateException("coding error");
						}
					}
					@Override
					public Iterable<URL> loadResources(String path) {
						throw new UnsupportedOperationException();
					}
				}) {
					@Override
					public void resolveDependencies() {
						// no need to parse catalog.xml for this fake module
					}
				};
			// whether catalog.xml has entries with name
			boolean hasCatalog = false;
			// catalog entries with content-type="xslt-package"
			Map<URI,XSLTPackage> xsltPackages = new HashMap<>();
			// dependency checks to be performed for a catalog entry when it has a name
			Map<URI,String> componentDependencyChecks = new HashMap<>();
			// dependency checks to be performed for a catalog entry when it has content-type="xslt-package"
			Map<String,String> xsltPackageDependencyChecks = new HashMap<>();
			// dependency checks to be performed for a catalog entry when it is an internal resource
			Map<String,String> resourceDependencyChecks = new HashMap<>();
			// whether some components (of type XSLT, XProc or RelaxNG) depend on an external component (xsl:import, xsl:include, p:import, cx:import, grammar:include)
			boolean componentChecks = false;
			// whether some components (of type XSLT) depend on a (internal) Java class
			boolean extensionFunctionChecks = false;
			// whether some components (of type XSLT) depend on an XSLT package (xsl:use-package)
			boolean xsltPackageChecks = false;
			Set<String> imports = new HashSet<>();
			for (CatalogEntry entry : catalogEntries) {
				if (entry.name != null)
					hasCatalog = true;
				if (entry.contentType == ContentType.XSLT_PACKAGE) {
					try {
						XSLTPackage p = new XSLTPackage(moduleUnderCompilation, entry.uri.toString(), xmlParser);
						xsltPackages.put(entry.uri, p);
					} catch (NoSuchFileException e) {
						throw new RuntimeException("Could not process catalog entry: resource not found: " + entry.uri.toString(), e);
					} catch (IllegalArgumentException e) {
						throw new RuntimeException("Could not process catalog entry: invalid XSLT package: " + entry.uri.toString(), e);
					}
				}
				Set<Dependency> dependencies; {
					try {
						if (entry.uri.toString().endsWith(".xpl"))
							dependencies = new XProcResource(moduleUnderCompilation, entry.uri.toString())
							                   .listDependencies(moduleRegistry, sourceRoots, xmlParser);
						else if (entry.uri.toString().endsWith(".xsl"))
							dependencies = new XSLTResource(moduleUnderCompilation, entry.uri.toString())
							                   .listDependencies(moduleRegistry, sourceRoots, xmlParser);
						else if (entry.uri.toString().endsWith(".rng"))
							dependencies = new RelaxNGResource(moduleUnderCompilation, entry.uri.toString())
							                   .listDependencies(moduleRegistry, xmlParser);
						else
							dependencies = null;
					} catch (NoSuchFileException e) {
						throw new RuntimeException("Could not process catalog entry: resource not found: " + entry.uri.toString(), e);
					}
				}
				if (dependencies != null && !dependencies.isEmpty()) {
					StringBuilder s = new StringBuilder();
					for (Dependency dependency : dependencies) {
						if (s.length() > 0)
							s.append("\n");
						if (dependency instanceof Component) {
							componentChecks = true;
							Component component = (Component)dependency;
							Version version = Version.parseVersion(component.getVersion());
							String versionRange = "[" + version + "," + new Version(version.getMajor() + 1, 0, 0) + ")";
							s.append("tryResolveComponent(\"" + component.getURI() + "\",\n");
							s.append("                    \"" + versionRange + "\");");
						} else if (dependency instanceof UseXSLTPackage) {
							xsltPackageChecks = true;
							UseXSLTPackage usePackage = (UseXSLTPackage)dependency;
							s.append("tryResolveXSLTPackage(\"" + usePackage.getName() + "\",\n");
							s.append("                      \"" + usePackage.getVersion() + "\");");
						} else if (dependency instanceof JavaDependency) {
							extensionFunctionChecks = true;
							s.append("tryResolveXPathExtensionFunction(\"" + ((JavaDependency)dependency).getClassName() + "\");");
						} else
							throw new IllegalStateException("Unknown dependency: " + dependency);
					}
					if (entry.name != null)
						componentDependencyChecks.put(entry.name, s.toString());
					else if (entry.contentType == ContentType.XSLT_PACKAGE)
						xsltPackageDependencyChecks.put(entry.uri.toString(), s.toString());
					else
						resourceDependencyChecks.put(entry.uri.toString(), s.toString());
				}
			}
			if (!hasCatalog)
				hasCatalog = catalog.getElementsByTagNameNS(CAT_PUBLIC.getNamespaceURI(), CAT_PUBLIC.getLocalPart()).getLength() > 0 ||
				             catalog.getElementsByTagNameNS(CAT_SYSTEM.getNamespaceURI(), CAT_SYSTEM.getLocalPart()).getLength() > 0 ||
				             catalog.getElementsByTagNameNS(CAT_REWRITE_URI.getNamespaceURI(), CAT_REWRITE_URI.getLocalPart()).getLength() > 0;
			StringBuilder result = new StringBuilder();
			imports.add("org.daisy.pipeline.modules.Module");
			imports.add("org.daisy.pipeline.xmlcatalog.XmlCatalogParser");
			imports.add("org.osgi.service.component.annotations.Reference");
			imports.add("org.osgi.service.component.annotations.ReferenceCardinality");
			imports.add("org.osgi.service.component.annotations.ReferencePolicy");
			imports.add("org.slf4j.Logger");
			imports.add("org.slf4j.LoggerFactory");
			result.append("@");
			if (!componentDependencyChecks.isEmpty())
				result.append("org.osgi.service.component.annotations.");
			else
				imports.add("org.osgi.service.component.annotations.Component");
			result.append("Component(\n");
			result.append("    name = \"org.daisy.pipeline.modules.impl." + className + "\",\n");
			result.append("    service = { Module.class }\n");
			result.append(")\n");
			result.append("public class " + className + " extends Module {\n");
			result.append("\n");
			result.append("    private static final Logger logger = LoggerFactory.getLogger(" + className + ".class);\n");
			result.append("\n");
			if (hasCatalog)
				result.append("    private boolean initialized = false;\n");
			result.append("    private XmlCatalogParser catalogParser;\n");
			if (!xsltPackageDependencyChecks.isEmpty()) {
				imports.add("javax.xml.stream.XMLInputFactory");
				result.append("    private XMLInputFactory xmlParser;\n");
			}
			if (extensionFunctionChecks) {
				imports.add("org.daisy.common.xpath.saxon.XPathFunctionRegistry");
				result.append("    private XPathFunctionRegistry xpathFunctions;\n");
			}
			if (xsltPackageChecks) {
				imports.add("java.util.List");
				imports.add("net.sf.saxon.trans.packages.PackageDetails");
				result.append("    private List<PackageDetails> packageDetails;\n");
			}
			if (componentChecks) {
				imports.add("org.daisy.pipeline.modules.ModuleRegistry");
				result.append("    private ModuleRegistry moduleRegistry;\n");
			}
			if (!componentDependencyChecks.isEmpty()) {
				imports.add("java.util.LinkedList");
				imports.add("org.daisy.pipeline.modules.Component");
				result.append("    private final LinkedList<Component> componentsToAdd;\n");
				result.append("    private final LinkedList<Component> componentsBeingAdded;\n");
			}
			if (!xsltPackageDependencyChecks.isEmpty()) {
				imports.add("java.util.LinkedList");
				result.append("    private final LinkedList<String> xsltPackagesToAdd;\n");
				result.append("    private final LinkedList<String> xsltPackagesBeingAdded;\n");
			}
			result.append("\n");
			result.append("    public " + className + "() {\n");
			result.append("        super(\"" + moduleName + "\",\n");
			result.append("              \"" + moduleVersion + "\",\n");
			result.append("              \"" + moduleTitle.replace("\"", "\\\"") + "\");\n");
			if (!componentDependencyChecks.isEmpty()) {
				imports.add("java.util.LinkedList");
				result.append("        componentsToAdd = new LinkedList<>();\n");
				result.append("        componentsBeingAdded = new LinkedList<>();\n");
			}
			if (!xsltPackageDependencyChecks.isEmpty()) {
				imports.add("java.util.LinkedList");
				result.append("        xsltPackagesToAdd = new LinkedList<>();\n");
				result.append("        xsltPackagesBeingAdded = new LinkedList<>();\n");
			}
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
			result.append("\n");
			if (!xsltPackageDependencyChecks.isEmpty()) {
				imports.add("javax.xml.stream.XMLInputFactory");
				result.append("    @Reference(\n");
				result.append("        name = \"XMLInputFactory\",\n");
				result.append("        unbind = \"-\",\n");
				result.append("        service = XMLInputFactory.class,\n");
				result.append("        cardinality = ReferenceCardinality.MANDATORY,\n");
				result.append("        policy = ReferencePolicy.STATIC\n");
				result.append("    )\n");
				result.append("    protected void setXMLInputFactory(XMLInputFactory parser) {\n");
				result.append("        xmlParser = parser;\n");
				result.append("    }\n");
				result.append("\n");
			}
			if (componentChecks) {
				imports.add("java.net.URI");
				imports.add("org.daisy.pipeline.modules.ModuleRegistry");
				imports.add("org.daisy.pipeline.modules.ResolutionException");
				result.append("    @Reference(\n");
				result.append("        name = \"ModuleRegistry\",\n");
				result.append("        unbind = \"-\",\n");
				result.append("        service = ModuleRegistry.class,\n");
				result.append("        cardinality = ReferenceCardinality.MANDATORY,\n");
				result.append("        policy = ReferencePolicy.STATIC\n");
				result.append("    )\n");
				result.append("    public void setModuleRegistry(ModuleRegistry registry) {\n");
				result.append("        moduleRegistry = registry;\n");
				result.append("    }\n");
				result.append("\n");
				result.append("    private void tryResolveComponent(String uri, String versionRange) throws ResolutionException {\n");
				result.append("        if (moduleRegistry.getModuleByComponent(URI.create(uri), versionRange) == null)\n");
				result.append("            throw new ResolutionException(\"Unresolved dependency \" + uri + \" with version=\" + versionRange);\n");
				result.append("    }\n");
				result.append("\n");
			}
			if (!componentDependencyChecks.isEmpty() || !xsltPackageDependencyChecks.isEmpty()) {
				imports.add("org.osgi.service.component.annotations.Activate");
				result.append("    @Activate\n");
				result.append("    protected void init() {\n");
				result.append("        logger.debug(\"Initializing module \" + getName());\n");
				if (!componentDependencyChecks.isEmpty()) {
					imports.add("java.net.URI");
					imports.add("org.slf4j.helpers.NOPLogger");
					result.append("        Module tmpModule = new Module(getName(), getVersion(), getTitle()) {\n");
					result.append("            @Override public void resolveDependencies() {}\n");
					result.append("            @Override public Logger getLogger() { return NOPLogger.NOP_LOGGER; }\n");
					result.append("        };\n");
					result.append("        Module.parseCatalog(tmpModule, catalogParser);\n");
					result.append("        for (String e : tmpModule.getEntities()) {\n");
					result.append("            addEntity(tmpModule.getEntity(e));\n");
					result.append("            logger.debug(\"Loaded entity: \" + e);\n");
					result.append("        }\n");
					result.append("        for (URI c : tmpModule.getComponents()) {\n");
					result.append("            components.put(c, () -> null); // will be replaced during resolveDependencies()\n");
					result.append("            componentsToAdd.add(tmpModule.getComponent(c));\n");
					result.append("        }\n");
				} else
					result.append("        Module.parseCatalog(this, catalogParser);\n");
				for (URI path : xsltPackages.keySet()) {
					XSLTPackage p = xsltPackages.get(path);
					if (xsltPackageDependencyChecks.containsKey(path.toString())) {
						result.append("        xsltPackagesToAdd.add(\"" + path + "\");\n");
					} else {
						imports.add("java.nio.file.NoSuchFileException");
						result.append("        try {\n");
						result.append("            addXSLTPackage(\"" + p.getName() + "\",\n");
						result.append("                           \"" + p.getVersion() + "\",\n");
						result.append("                           \"" + path + "\");\n");
						result.append("        } catch (NoSuchFileException e) {\n");
						result.append("            logger.warn(\"XSLT package " + p.getName() + " can not be loaded: \" + e.getMessage());\n");
						result.append("        }\n");
					}
				}
				result.append("        logger.debug(\"Module \" + getName() + \" initialized, but dependencies have not been resolved yet\");\n");
				result.append("    }\n");
				result.append("\n");
				result.append("    // Not calling resolveDependencies() during object construction yet because it depends\n");
				result.append("    // on other modules, and other modules may depend on this module. resolveDependencies()\n");
				result.append("    // will be called when the object gets bound in ModuleRegistry. The method may recursively\n");
				result.append("    // call itself. It copes with that by activating components in a different order\n");
				result.append("    // when called recursively. ModuleRegistry has protection against endless recursion.\n");
				result.append("    @Override\n");
				result.append("    public void resolveDependencies() {\n");
				result.append("        if (!initialized) {\n");
				if (!componentDependencyChecks.isEmpty() && !xsltPackageDependencyChecks.isEmpty())
					result.append("            boolean recursive = !componentsBeingAdded.isEmpty() || !xsltPackagesBeingAdded.isEmpty();\n");
				else if (!componentDependencyChecks.isEmpty())
					result.append("            boolean recursive = !componentsBeingAdded.isEmpty();\n");
				else
					result.append("            boolean recursive = !xsltPackagesBeingAdded.isEmpty();\n");
				result.append("            if (recursive)\n");
				result.append("                logger.debug(\"Resolving dependencies of module \" + getName() + \" (continuing in recursive call)\");\n");
				result.append("            else\n");
				result.append("                logger.debug(\"Resolving dependencies of module \" + getName());\n");
				if (!componentDependencyChecks.isEmpty()) {
					imports.add("org.daisy.pipeline.modules.Component");
					result.append("            while (true) {\n");
					result.append("                if (componentsToAdd.isEmpty()) {\n");
					result.append("                    if (componentsBeingAdded.isEmpty())\n");
					result.append("                        break;\n");
					result.append("                    componentsToAdd.addAll(componentsBeingAdded);\n");
					result.append("                    componentsBeingAdded.clear();\n");
					result.append("                }\n");
					result.append("                Component c = componentsToAdd.poll();\n");
					result.append("                componentsBeingAdded.add(c);\n");
					result.append("                if (addComponent(c)) // may call resolveDependencies() recursively\n");
					result.append("                    logger.debug(\"Loaded component: \" + c.getURI());\n");
					result.append("                componentsBeingAdded.remove(c);\n");
					result.append("                componentsToAdd.remove(c);\n");
					result.append("            }\n");
				}
				if (!xsltPackageDependencyChecks.isEmpty()) {
					imports.add("java.nio.file.NoSuchFileException");
					result.append("            while (true) {\n");
					result.append("                if (xsltPackagesToAdd.isEmpty()) {\n");
					result.append("                    if (xsltPackagesBeingAdded.isEmpty())\n");
					result.append("                        break;\n");
					result.append("                    xsltPackagesToAdd.addAll(xsltPackagesBeingAdded);\n");
					result.append("                    xsltPackagesBeingAdded.clear();\n");
					result.append("                }\n");
					result.append("                String p = xsltPackagesToAdd.poll();\n");
					result.append("                logger.debug(\"Loading XSLT package in module \" + getName() + \": \" + p);\n");
					result.append("                xsltPackagesBeingAdded.add(p);\n");
					result.append("                try {\n");
					result.append("                    addXSLTPackage(p, xmlParser); // may call resolveDependencies() recursively\n");
					result.append("                } catch (NoSuchFileException e) {\n");
					result.append("                    logger.warn(\"XSLT package can not be loaded: \" + e.getMessage());\n");
					result.append("                } catch (IllegalArgumentException e) {\n");
					result.append("                    throw new IllegalStateException(); // should not happen: file validated during compilation\n");
					result.append("                }\n");
					result.append("                xsltPackagesBeingAdded.remove(p);\n");
					result.append("                xsltPackagesToAdd.remove(p);\n");
					result.append("            }\n");
				}
				result.append("            if (!initialized)\n");
				result.append("                logger.debug(\"Done resolving dependencies of module \" + getName());\n");
				result.append("            initialized = true;\n");
				result.append("        }\n");
				result.append("    }\n");
				if (!componentDependencyChecks.isEmpty()) {
					imports.add("org.daisy.pipeline.modules.Component");
					imports.add("org.daisy.pipeline.modules.ResolutionException");
					result.append("\n");
					result.append("    @Override\n");
					result.append("    protected boolean addComponent(Component component) {\n");
					result.append("        String name = component.getURI().toString();\n");
					result.append("        try {\n");
					boolean first = true;
					for (URI componentName : componentDependencyChecks.keySet()) {
						if (first)
							result.append("            ");
						else
							result.append(" else ");
						first = false;
						result.append("if (\"" + componentName + "\".equals(name)) {\n");
						for (String s : componentDependencyChecks.get(componentName).split("\n"))
							result.append("                ").append(s).append("\n");
						result.append("            }");
					}
					result.append("\n");
					result.append("        } catch (ResolutionException re) {\n");
					result.append("            logger.warn(\"Component \" + component.getURI() + \" can not be loaded: \" + re.getMessage());\n");
					result.append("            return false;\n");
					result.append("        }\n");
					result.append("        return super.addComponent(component);\n");
					result.append("    }\n");
				}
			} else {
				if (hasCatalog || !xsltPackages.isEmpty()) {
					imports.add("org.osgi.service.component.annotations.Activate");
					result.append("    @Activate\n");
					result.append("    protected void init() {\n");
					result.append("        logger.debug(\"Initializing module \" + getName());\n");
					if (hasCatalog)
						result.append("        Module.parseCatalog(this, catalogParser);\n");
					for (URI path : xsltPackages.keySet()) {
						imports.add("java.nio.file.NoSuchFileException");
						XSLTPackage p = xsltPackages.get(path);
						result.append("        try {\n");
						result.append("            addXSLTPackage(\"" + p.getName() + "\",\n");
						result.append("                           \"" + p.getVersion() + "\",\n");
						result.append("                           \"" + path + "\");\n");
						result.append("        } catch (NoSuchFileException e) {\n");
						result.append("            logger.warn(\"XSLT package " + p.getName() + " can not be loaded: \" + e.getMessage());\n");
						result.append("        }\n");
					}
					result.append("        logger.debug(\"Module \" + getName() + \" initialized\");\n");
					result.append("    }\n");
					result.append("\n");
				}
				result.append("    @Override\n");
				result.append("    public void resolveDependencies() {\n");
				result.append("        logger.debug(\"No dependencies to resolve for module \" + getName());\n");
				result.append("    }\n");
			}
			if (extensionFunctionChecks) {
				imports.add("java.nio.file.NoSuchFileException");
				imports.add("net.sf.saxon.lib.ExtensionFunctionDefinition");
				imports.add("org.daisy.common.xpath.saxon.XPathFunctionRegistry");
				imports.add("org.daisy.pipeline.modules.ResolutionException");
				result.append("\n");
				result.append("    @Reference(\n");
				result.append("        name = \"XPathFunctionRegistry\",\n");
				result.append("        unbind = \"-\",\n");
				result.append("        service = XPathFunctionRegistry.class,\n");
				result.append("        cardinality = ReferenceCardinality.MANDATORY,\n");
				result.append("        policy = ReferencePolicy.STATIC\n");
				result.append("    )\n");
				result.append("    public void setXPathFunctionRegistry(XPathFunctionRegistry registry) {\n");
				result.append("        xpathFunctions = registry;\n");
				result.append("    }\n");
				result.append("\n");
				result.append("    private void tryResolveXPathExtensionFunction(String className) throws ResolutionException {\n");
				result.append("        for (ExtensionFunctionDefinition d : xpathFunctions.getFunctions()) {\n");
				result.append("            if (className.equals(d.getFunctionQName().getURI())) {\n");
				result.append("                try {\n");
				result.append("                    getResource(\"../\" + className.replace('.', '/') + \".class\");\n");
				result.append("                    return;\n");
				result.append("                } catch (NoSuchFileException e) {\n");
				result.append("                    logger.debug(\n");
				result.append("                        \"Class \" + className + \" expected to be in the same module (\" + getName() + \").\");\n");
				result.append("                }\n");
				result.append("            }\n");
				result.append("        }\n");
				result.append("        throw new ResolutionException(\n");
				result.append("            \"Unresolved Java dependency: no class \" + className + \" found in module \" + getName());\n");
				result.append("    }\n");
			}
			if (xsltPackageChecks) {
				imports.add("java.util.ArrayList");
				imports.add("net.sf.saxon.style.PackageVersion");
				imports.add("net.sf.saxon.style.PackageVersionRanges");
				imports.add("net.sf.saxon.trans.packages.PackageDetails");
				imports.add("net.sf.saxon.trans.XPathException");
				imports.add("org.daisy.pipeline.modules.ResolutionException");
				imports.add("org.daisy.pipeline.modules.XSLTPackage");
				result.append("\n");
				result.append("    @Reference(\n");
				result.append("        name = \"PackageDetails\",\n");
				result.append("        unbind = \"-\",\n");
				result.append("        service = PackageDetails.class,\n");
				result.append("        cardinality = ReferenceCardinality.MULTIPLE,\n");
				result.append("        policy = ReferencePolicy.STATIC\n");
				result.append("    )\n");
				result.append("    public void addPackageDetails(PackageDetails pack) {\n");
				result.append("        if (packageDetails == null)\n");
				result.append("            packageDetails = new ArrayList<>();\n");
				result.append("        packageDetails.add(pack);\n");
				result.append("    }\n");
				result.append("\n");
				result.append("    private void tryResolveXSLTPackage(String name, String versionRange) throws ResolutionException{\n");
				result.append("        if (packageDetails != null) {\n");
				result.append("            for (PackageDetails pack : packageDetails) {\n");
				result.append("                if (name.equals(pack.nameAndVersion.packageName)) {\n");
				result.append("                    if (versionRange != null) {\n");
				result.append("                        // see https://www.w3.org/TR/xslt-30/#package-versions\n");
				result.append("                        try {\n");
				result.append("                            if (new PackageVersionRanges(versionRange).contains(pack.nameAndVersion.packageVersion));\n");
				result.append("                                return true;\n");
				result.append("                            else\n");
				result.append("                                logger.debug(\"Version of \" + name + \" (\" + pack.nameAndVersion.packageVersion + \") \n");
				result.append("                                             + \"not in requested range (\" + versionRange + \")\");\n");
				result.append("                        } catch (XPathException e) {\n");
				result.append("                            \n");
				result.append("                        }\n");
				result.append("                    } else {\n");
				result.append("                        return;\n");
				result.append("                    }\n");
				result.append("                }\n");
				result.append("            }\n");
				result.append("        }\n");
				result.append("        if (getModuleByXSLTPackage(name, versionRange) != null)\n");
				result.append("            return;\n");
				result.append("        throw new ResolutionException(\"Unresolved XSLT package \" + name + \" with version=\" + versionRange);\n");
				result.append("    }\n");
				result.append("\n");
				result.append("    public Module getModuleByXSLTPackage(String name, String versionRange) {\n");
				result.append("        Module module = moduleRegistry.getModuleByXSLTPackage(name);\n");
				result.append("        if (module != null && versionRange != null) {\n");
				result.append("            XSLTPackage pack = module.getXSLTPackage(name);\n");
				result.append("            if (pack == null)\n");
				result.append("                throw new IllegalStateException(\"coding error\"); // can not happen\n");
				result.append("            PackageVersion version; {\n");
				result.append("                try {\n");
				result.append("                    version = new PackageVersion(pack.getVersion());\n");
				result.append("                } catch (XPathException e) {\n");
				result.append("                    logger.debug(\"Version of \" + name + \" can not be parsed: \" + pack.getVersion());\n");
				result.append("                    return null;\n");
				result.append("                }\n");
				result.append("            }\n");
				result.append("            try {\n");
				result.append("                if (!(new PackageVersionRanges(versionRange).contains(version))) {\n");
				result.append("                    logger.debug(\"Version of \" + name + \" (\" + version + \") not in requested range (\" + versionRange + \")\");\n");
				result.append("                    return null;\n");
				result.append("                }\n");
				result.append("            } catch (XPathException e) {\n");
				result.append("                logger.debug(\"Version range can not be parsed: \" + versionRange);\n");
				result.append("                return null;\n");
				result.append("            }\n");
				result.append("        }\n");
				result.append("        return module;\n");
				result.append("    }\n");
			}
			if (!resourceDependencyChecks.isEmpty() || !xsltPackageDependencyChecks.isEmpty()) {
				imports.add("java.net.URL");
				imports.add("java.nio.file.NoSuchFileException");
				imports.add("org.daisy.pipeline.modules.ResolutionException");
				resourceDependencyChecks.putAll(xsltPackageDependencyChecks);
				result.append("\n");
				for (int i = 1; i <= resourceDependencyChecks.size(); i++) {
					result.append("    private URL resource" + i + " = null;\n");
				}
				result.append("\n");
				result.append("    @Override\n");
				result.append("    public URL getResource(String path) throws NoSuchFileException {\n");
				result.append("        URL url = super.getResource(path);\n");
				result.append("        try {\n");
				int i = 1;
				for (String path : resourceDependencyChecks.keySet()) {
					result.append("            if (resource" + i + " == null)\n");
					result.append("                resource" + i + " = super.getResource(\"" + path + "\");\n");
					result.append("            if (url.equals(resource" + i + ")) {\n");
					for (String s : resourceDependencyChecks.get(path).split("\n")) {
						result.append("                ").append(s).append("\n");
					}
					result.append("                return url;\n");
					result.append("            }\n");
					i++;
				}
				result.append("        } catch (ResolutionException re) {\n");
				result.append("            throw new NoSuchFileException(\n");
				result.append("                \"Resource \" + path + \" in module \" + getName() + \" can not be loaded: \"");
				result.append("                + re.getMessage());\n");
				result.append("        }\n");
				result.append("        return url;\n");
				result.append("    }\n");
			}
			result.append("}\n");
			// insert package and imports
			if (imports.size() > 0) {
				List<String> sortedImports = new ArrayList<>(imports);
				Collections.sort(sortedImports);
				String lastPrefix = null;
				StringBuilder s = new StringBuilder();
				for (String i : sortedImports) {
					String prefix = i.replaceAll("^([^.]+)\\..*$", "$1");
					if (lastPrefix == null)
						lastPrefix = prefix;
					else if (!lastPrefix.equals(prefix)) {
						lastPrefix = prefix;
						s.append("\n");
					}
					s.append("import ").append(i).append(";\n");
				}
				result.insert(0, "\n").insert(0, s);
			}
			result.insert(0, "package org.daisy.pipeline.modules.impl;\n\n");
			logger.debug("Successfully generated class " + className);
			return result.toString();
		}
	}

	private static final String XML_CATALOG_NS = "urn:oasis:names:tc:entity:xmlns:xml:catalog";
	private static final QName CAT_URI = new QName(XML_CATALOG_NS, "uri");
	private static final QName CAT_PUBLIC = new QName(XML_CATALOG_NS, "public");
	private static final QName CAT_SYSTEM = new QName(XML_CATALOG_NS, "system");
	private static final QName CAT_REWRITE_URI = new QName(XML_CATALOG_NS, "rewriteURI");
	private static final QName _NAME = new QName("name");
	private static final QName _URI = new QName("uri");
	private static final String PIPELINE_EXT_NS = "http://www.daisy.org/ns/pipeline";
	private static final QName PX_CONTENT_TYPE = new QName(PIPELINE_EXT_NS, "content-type");

	private static Set<CatalogEntry> parseCatalog(Element catalog) {
		Set<CatalogEntry> entries = new HashSet<>();
		NodeList children = catalog.getElementsByTagNameNS(CAT_URI.getNamespaceURI(), CAT_URI.getLocalPart());
		for (int i = 0; i < children.getLength(); i++) {
			Element e = (Element)children.item(i);
			Attr name = e.getAttributeNodeNS(_NAME.getNamespaceURI(), _NAME.getLocalPart());
			Attr uri = e.getAttributeNodeNS(_URI.getNamespaceURI(), _URI.getLocalPart());
			Attr contentType = e.getAttributeNodeNS(PX_CONTENT_TYPE.getNamespaceURI(), PX_CONTENT_TYPE.getLocalPart());
			entries.add(new CatalogEntry(name != null ? URI.create(name.getValue()) : null,
			                             uri != null ? URI.create(uri.getValue()) : null,
			                             contentType != null ?  ContentType.create(contentType.getValue()) : null));
		}
		return entries;
	}

	private static class CatalogEntry {
		public final URI name;
		public final URI uri;
		public final ContentType contentType;
		public CatalogEntry(URI name, URI uri, ContentType contentType) {
			if (uri == null)
				throw new IllegalArgumentException("uri must not be null");
			this.name = name;
			this.uri = uri;
			this.contentType = contentType;
		}
	}

	private static enum ContentType {

		SCRIPT("script"),
		DATA_TYPE("data-type"),
		XSLT_PACKAGE("xslt-package"),
		PARAMS("params"),
		CALABASH_CONFIG("calabash-config"),
		LIBLOUIS_TABLES("liblouis-tables"),
		LIBHYPHEN_TABLES("libhyphen-tables"),
		USER_AGENT_STYLESHEET("user-agent-stylesheet");

		private final String type;

		private ContentType(String type) {
			this.type = type;
		}

		public static ContentType create(String type) {
			for (ContentType t : ContentType.values())
				if (t.type.equals(type))
					return t;
			throw new IllegalArgumentException("Not a valid content-type: " + type);
		}
	}

	private static ModuleRegistry getModuleRegistry(ClassLoader classLoader) {
		ClassLoader restoreClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(classLoader);
			for (ModuleRegistry r : ServiceLoader.load(ModuleRegistry.class))
				return r;
			throw new RuntimeException("No ModuleRegistry found");
		} finally {
			Thread.currentThread().setContextClassLoader(restoreClassLoader);
		}
	}
}
