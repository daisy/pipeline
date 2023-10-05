package org.daisy.pipeline.maven.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
					public void init() {
						// no need to parse catalog.xml for this fake module
					}
				};
			boolean hasCatalog = false;
			Map<URI,XSLTPackage> xsltPackages = new HashMap<>();
			Map<URI,String> componentDependencyChecks = new HashMap<>();
			Map<String,String> xsltPackageDependencyChecks = new HashMap<>();
			Map<String,String> resourceDependencyChecks = new HashMap<>();
			boolean componentChecks = false;
			boolean extensionFunctionChecks = false;
			boolean xsltPackageChecks = false;
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
					String breakStatement = entry.name != null ? "return false;" : "throw new NoSuchFileException(\"file does not exist\");";
					for (Dependency dependency : dependencies) {
						if (s.length() > 0)
							s.append("\n");
						if (dependency instanceof Component) {
							componentChecks = true;
							Component component = (Component)dependency;
							Version version = Version.parseVersion(component.getVersion());
							String versionRange = "[" + version + "," + new Version(version.getMajor() + 1, 0, 0) + ")";
							s.append("if (!canResolveComponent(\"" + component.getURI() + "\",\n");
							s.append("                         \"" + versionRange + "\"))\n");
							s.append("    " + breakStatement);
						} else if (dependency instanceof UseXSLTPackage) {
							xsltPackageChecks = true;
							UseXSLTPackage usePackage = (UseXSLTPackage)dependency;
							s.append("if (!canResolveXSLTPackage(\"" + usePackage.getName() + "\",\n");
							s.append("                           \"" + usePackage.getVersion() + "\"))\n");
							s.append("    " + breakStatement);
						} else if (dependency instanceof JavaDependency) {
							extensionFunctionChecks = true;
							s.append("if (!canResolveXPathExtensionFunction(\"" + ((JavaDependency)dependency).getClassName() + "\"))\n");
							s.append("    " + breakStatement);
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
			result.append("package org.daisy.pipeline.modules.impl;\n");
			result.append("\n");
			{
				StringBuilder imports = new StringBuilder();
				if (componentChecks)
					imports.append("import java.net.URI;\n");
				if (!resourceDependencyChecks.isEmpty() || !xsltPackageDependencyChecks.isEmpty())
					imports.append("import java.net.URL;\n");
				if (!resourceDependencyChecks.isEmpty() || !xsltPackageDependencyChecks.isEmpty() || extensionFunctionChecks)
					imports.append("import java.nio.file.NoSuchFileException;\n");
				if (xsltPackageChecks)
					imports.append("import java.util.ArrayList;\n");
				if (componentChecks)
					imports.append("import java.util.HashMap;\n");
				if (!componentDependencyChecks.isEmpty() || !xsltPackageDependencyChecks.isEmpty())
					imports.append("import java.util.LinkedList;\n");
				if (xsltPackageChecks)
					imports.append("import java.util.List;\n");
				if (componentChecks)
					imports.append("import java.util.Map;\n");
				if (imports.length() > 0)
					result.append(imports.toString()).append("\n");
			}
			if (!xsltPackageDependencyChecks.isEmpty())
				result.append("import javax.xml.stream.XMLInputFactory;\n").append("\n");
			{
				StringBuilder imports = new StringBuilder();
				if (extensionFunctionChecks)
					imports.append("import net.sf.saxon.lib.ExtensionFunctionDefinition;\n");
				if (xsltPackageChecks) {
					imports.append("import net.sf.saxon.style.PackageVersion;\n");
					imports.append("import net.sf.saxon.style.PackageVersionRanges;\n");
					imports.append("import net.sf.saxon.trans.packages.PackageDetails;\n");
					imports.append("import net.sf.saxon.trans.XPathException;\n");
				}
				if (imports.length() > 0)
					result.append(imports.toString()).append("\n");
			}
			if (extensionFunctionChecks)
				result.append("import org.daisy.common.xpath.saxon.XPathFunctionRegistry;\n");
			if (componentChecks || !componentDependencyChecks.isEmpty()) {
				result.append("import org.daisy.pipeline.modules.Component;\n");
				result.append("import org.daisy.pipeline.modules.Entity;\n");
			}
			result.append("import org.daisy.pipeline.modules.Module;\n");
			if (componentChecks || xsltPackageChecks)
				result.append("import org.daisy.pipeline.modules.ModuleRegistry;\n");
			if (xsltPackageChecks)
				result.append("import org.daisy.pipeline.modules.XSLTPackage;\n");
			result.append("import org.daisy.pipeline.xmlcatalog.XmlCatalogParser;\n");
			result.append("\n");
			if (componentChecks) {
				result.append("import org.osgi.framework.Version;\n");
				result.append("import org.osgi.framework.VersionRange;\n");
			} else if (hasCatalog)
				result.append("import org.osgi.service.component.annotations.Activate;\n");
			if (!componentChecks && componentDependencyChecks.isEmpty())
				result.append("import org.osgi.service.component.annotations.Component;\n");
			result.append("import org.osgi.service.component.annotations.Reference;\n");
			result.append("import org.osgi.service.component.annotations.ReferenceCardinality;\n");
			result.append("import org.osgi.service.component.annotations.ReferencePolicy;\n");
			result.append("\n");
			if (!componentDependencyChecks.isEmpty())
				result.append("import static org.slf4j.helpers.NOPLogger.NOP_LOGGER;\n");
			result.append("import org.slf4j.Logger;\n");
			result.append("import org.slf4j.LoggerFactory;\n");
			result.append("\n");
			result.append("@");
			if (componentChecks || !componentDependencyChecks.isEmpty())
				result.append("org.osgi.service.component.annotations.");
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
			if (!xsltPackageDependencyChecks.isEmpty())
				result.append("    private XMLInputFactory xmlParser;\n");
			if (extensionFunctionChecks)
				result.append("    private XPathFunctionRegistry xpathFunctions;\n");
			if (xsltPackageChecks)
				result.append("    private List<PackageDetails> packageDetails;\n");
			if (componentChecks)
				result.append("    private ModuleRegistry moduleRegistry;\n");
			if (!componentDependencyChecks.isEmpty()) {
				result.append("    private final LinkedList<Component> componentsToAdd;\n");
				result.append("    private final LinkedList<Component> componentsBeingAdded;\n");
			}
			if (!xsltPackageDependencyChecks.isEmpty()) {
				result.append("    private final LinkedList<String> xsltPackagesToAdd;\n");
				result.append("    private final LinkedList<String> xsltPackagesBeingAdded;\n");
			}
			result.append("\n");
			result.append("    public " + className + "() {\n");
			result.append("        super(\"" + moduleName + "\",\n");
			result.append("              \"" + moduleVersion + "\",\n");
			result.append("              \"" + moduleTitle.replace("\"", "\\\"") + "\");\n");
			if (!componentDependencyChecks.isEmpty()) {
				result.append("        componentsToAdd = new LinkedList<>();\n");
				result.append("        componentsBeingAdded = new LinkedList<>();\n");
			}
			if (!xsltPackageDependencyChecks.isEmpty()) {
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
				result.append("    private boolean canResolveComponent(String uri, String versionRange) {\n");
				result.append("        if (moduleRegistry.getModuleByComponent(URI.create(uri), versionRange) == null) {\n");
				result.append("            logger.warn(\"Component can not be started: unresolved dependency \" + uri + \" with version=\" + versionRange);\n");
				result.append("            return false;\n");
				result.append("        } else\n");
				result.append("            return true;\n");
				result.append("    }\n");
				result.append("\n");
			}
			if (!componentDependencyChecks.isEmpty() || !xsltPackageDependencyChecks.isEmpty()) {
				result.append("    // Not calling init() during object construction yet because it depends on other\n");
				result.append("    // modules, and other modules may depend on this module. The init() method will be\n");
				result.append("    // called when the object gets bound in ModuleRegistry. The method may recursively\n");
				result.append("    // call itself. It copes with that by activating components in a different order\n");
				result.append("    // when called recursively. ModuleRegistry has protection against endless recursion.\n");
				result.append("    @Override\n");
				result.append("    public void init() {\n");
				result.append("        if (!initialized) {\n");
				if (!componentDependencyChecks.isEmpty() && !xsltPackageDependencyChecks.isEmpty())
					result.append("            boolean recursive = !componentsBeingAdded.isEmpty() || !xsltPackagesBeingAdded.isEmpty();\n");
				else if (!componentDependencyChecks.isEmpty())
					result.append("            boolean recursive = !componentsBeingAdded.isEmpty();\n");
				else
					result.append("            boolean recursive = !xsltPackagesBeingAdded.isEmpty();\n");
				result.append("            if (recursive)\n");
				result.append("                logger.trace(\"Initializing (continuing in recursive call)\");\n");
				result.append("            else\n");
				result.append("                logger.trace(\"Initializing\");\n");
				result.append("            if (!recursive) {\n");
				if (!componentDependencyChecks.isEmpty()) {
					result.append("                Module tmpModule = new Module(getName(), getVersion(), getTitle()) {\n");
					result.append("                    @Override public void init() {}\n");
					result.append("                    @Override public Logger getLogger() { return NOP_LOGGER; }\n");
					result.append("                };\n");
					result.append("                Module.parseCatalog(tmpModule, catalogParser);\n");
					result.append("                for (Entity e : tmpModule.getEntities()) {\n");
					result.append("                    logger.trace(\"Adding entity: \" + e.getPublicId());\n");
					result.append("                    addEntity(e);\n");
					result.append("                }\n");
					result.append("                for (Component c : tmpModule.getComponents())\n");
					result.append("                    componentsToAdd.add(c);\n");
				} else if (hasCatalog)
					result.append("                Module.parseCatalog(this, catalogParser);\n");
				for (URI path : xsltPackages.keySet()) {
					XSLTPackage p = xsltPackages.get(path);
					if (xsltPackageDependencyChecks.containsKey(path.toString())) {
						result.append("                xsltPackagesToAdd.add(\"" + path + "\");\n");
					} else {
						result.append("                try {\n");
						result.append("                    addXSLTPackage(\"" + p.getName() + "\",\n");
						result.append("                                   \"" + p.getVersion() + "\",\n");
						result.append("                                   \"" + path + "\");\n");
						result.append("                } catch (NoSuchFileException e) {\n");
						result.append("                    logger.warn(\"Component can not be started: resource not found: \" + path);\n");
						result.append("                }\n");
					}
				}
				result.append("            }\n");
				if (!componentDependencyChecks.isEmpty()) {
					result.append("            while (true) {\n");
					result.append("                if (componentsToAdd.isEmpty()) {\n");
					result.append("                    if (componentsBeingAdded.isEmpty())\n");
					result.append("                        break;\n");
					result.append("                    componentsToAdd.addAll(componentsBeingAdded);\n");
					result.append("                    componentsBeingAdded.clear();\n");
					result.append("                }\n");
					result.append("                Component c = componentsToAdd.poll();\n");
					result.append("                logger.trace(\"Adding component: \" + c.getURI());\n");
					result.append("                componentsBeingAdded.add(c);\n");
					result.append("                addComponent(c); // may call init() recursively\n");
					result.append("                componentsBeingAdded.remove(c);\n");
					result.append("                componentsToAdd.remove(c);\n");
					result.append("            }\n");
				}
				if (!xsltPackageDependencyChecks.isEmpty()) {
					result.append("            while (true) {\n");
					result.append("                if (xsltPackagesToAdd.isEmpty()) {\n");
					result.append("                    if (xsltPackagesBeingAdded.isEmpty())\n");
					result.append("                        break;\n");
					result.append("                    xsltPackagesToAdd.addAll(xsltPackagesBeingAdded);\n");
					result.append("                    xsltPackagesBeingAdded.clear();\n");
					result.append("                }\n");
					result.append("                String p = xsltPackagesToAdd.poll();\n");
					result.append("                logger.trace(\"Adding XSLT package: \" + p);\n");
					result.append("                xsltPackagesBeingAdded.add(p);\n");
					result.append("                try {\n");
					result.append("                    addXSLTPackage(p, xmlParser); // may call init() recursively\n");
					result.append("                } catch (NoSuchFileException e) {\n");
					result.append("                    logger.warn(\"Component can not be started: resource not found: \" + p);\n");
					result.append("                } catch (IllegalArgumentException e) {\n");
					result.append("                    throw new IllegalStateException(); // should not happen: file validated during compilation\n");
					result.append("                }\n");
					result.append("                xsltPackagesBeingAdded.remove(p);\n");
					result.append("                xsltPackagesToAdd.remove(p);\n");
					result.append("            }\n");
				}
				result.append("            if (!initialized)\n");
				result.append("                logger.debug(\"Initialized\");\n");
				result.append("            initialized = true;\n");
				result.append("        }\n");
				result.append("    }\n");
				if (!componentDependencyChecks.isEmpty()) {
					result.append("\n");
					result.append("    @Override\n");
					result.append("    protected boolean addComponent(Component component) {\n");
					result.append("        String name = component.getURI().toString();\n");
					boolean first = true;
					for (URI componentName : componentDependencyChecks.keySet()) {
						result.append("        ");
						if (!first)
							result.append(" else ");
						first = false;
						result.append("if (\"" + componentName + "\".equals(name)) {\n");
						for (String s : componentDependencyChecks.get(componentName).split("\n"))
							result.append("            ").append(s).append("\n");
						result.append("        }");
					}
					result.append("\n");
					result.append("        return super.addComponent(component);\n");
					result.append("    }\n");
				}
			} else if (hasCatalog || !xsltPackages.isEmpty()) {
				result.append("    @Activate\n");
				result.append("    @Override\n");
				result.append("    public void init() {\n");
				result.append("        if (!initialized) {\n");
				result.append("            logger.trace(\"Initializing\");\n");
				if (hasCatalog)
					result.append("            Module.parseCatalog(this, catalogParser);\n");
				for (URI path : xsltPackages.keySet()) {
					XSLTPackage p = xsltPackages.get(path);
					result.append("            addXSLTPackage(\"" + p.getName() + "\",\n");
					result.append("                           \"" + p.getVersion() + "\",\n");
					result.append("                           \"" + path + "\");\n");
				}
				result.append("            initialized = true;\n");
				result.append("            logger.trace(\"Initialized\");\n");
				result.append("        }\n");
				result.append("    }\n");
			} else {
				result.append("    @Override\n");
				result.append("    public void init() {\n");
				result.append("        logger.trace(\"Nothing to initialize\");\n");
				result.append("    }\n");
			}
			if (extensionFunctionChecks) {
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
				result.append("    private boolean canResolveXPathExtensionFunction(String className) {\n");
				result.append("        for (ExtensionFunctionDefinition d : xpathFunctions.getFunctions()) {\n");
				result.append("            if (className.equals(d.getFunctionQName().getURI())) {\n");
				result.append("                try {\n");
				result.append("                    getResource(\"../\" + className.replace('.', '/') + \".class\");\n");
				result.append("                    return true;\n");
				result.append("                } catch (NoSuchFileException e) {\n");
				result.append("                    logger.debug(\"Class \" + className + \" expected to be in the same module.\");\n");
				result.append("                }\n");
				result.append("            }\n");
				result.append("        }\n");
				result.append("        logger.warn(\"Component can not be started: unresolved Java dependency \" + className);\n");
				result.append("        return false;\n");
				result.append("    }\n");
			}
			if (xsltPackageChecks) {
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
				result.append("    private boolean canResolveXSLTPackage(String name, String versionRange) {\n");
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
				result.append("                        return true;\n");
				result.append("                    }\n");
				result.append("                }\n");
				result.append("            }\n");
				result.append("        }\n");
				result.append("        if (getModuleByXSLTPackage(name, versionRange) != null)\n");
				result.append("            return true;\n");
				result.append("        logger.warn(\"Component can not be started: unresolved XSLT package \" + name + \" with version=\" + versionRange);\n");
				result.append("        return false;\n");
				result.append("    }\n");

				// FIXME: move to DefaultModuleRegistry
				// => but should not depend on Saxon: port code?

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
				resourceDependencyChecks.putAll(xsltPackageDependencyChecks);
				result.append("\n");
				for (int i = 1; i <= resourceDependencyChecks.size(); i++) {
					result.append("    private URL resource" + i + " = null;\n");
				}
				result.append("\n");
				result.append("    @Override\n");
				result.append("    public URL getResource(String path) throws NoSuchFileException {\n");
				result.append("        URL url = super.getResource(path);\n");
				int i = 1;
				for (String path : resourceDependencyChecks.keySet()) {
					result.append("        if (resource" + i + " == null)\n");
					result.append("            resource" + i + " = super.getResource(\"" + path + "\");\n");
					result.append("        if (url.equals(resource" + i + ")) {\n");
					for (String s : resourceDependencyChecks.get(path).split("\n")) {
						result.append("            ").append(s).append("\n");
					}
					result.append("            return url;\n");
					result.append("        }\n");
					i++;
				}
				result.append("        return url;\n");
				result.append("    }\n");
			}
			result.append("}\n");
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
		LIBHYPHEN_TABLES("libhyphen-tables");

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
