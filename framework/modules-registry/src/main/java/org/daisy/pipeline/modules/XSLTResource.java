package org.daisy.pipeline.modules;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLInputFactory;

import org.daisy.common.file.URLs;

public class XSLTResource {

	private static final String XSLT_NS = "http://www.w3.org/1999/XSL/Transform";
	static final QName XSL_STYLESHEET = new QName(XSLT_NS, "stylesheet");
	static final QName XSL_PACKAGE = new QName(XSLT_NS, "package");
	private static final QName XSL_IMPORT = new QName(XSLT_NS, "import");
	private static final QName XSL_INCLUDE = new QName(XSLT_NS, "include");
	private static final QName XSL_USE_PACKAGE = new QName(XSLT_NS, "use-package");
	private static final QName _HREF = new QName("href");
	protected static final QName _NAME = new QName("name");
	protected static final QName _PACKAGE_VERSION = new QName("package-version");

	private final URL resource;
	private final Module module;

	public XSLTResource(Module module, String path) throws NoSuchFileException {
		this.resource = module.getResource(path);
		this.module = module;
	}

	public XSLTResource(XSLTResource resource) {
		this.resource = resource.resource;
		this.module = resource.module;
	}

	public XSLTResource(Component component) {
		try {
			this.resource = component.getResource().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException("coding error");
		}
		this.module = component.getModule();
	}

	public URL getResource() {
		return resource;
	}

	/**
	 * List the dependencies of this resource.
	 *
	 * This recursively resolves all <code>xsl:import</code> and <code>xsl:include</code> that
	 * reference a component (either in the same module or in another one). It also resolves
	 * <code>xsl:use-package</code> and it resolves XPath extension functions defined in Java. Other
	 * dependencies are ignored.
	 *
	 * The dependencies may include (external) {@link Component}, {@link UseXSLTPackage} and
	 * (internal) {@link JavaDependency}. Imported/included resources from the same module are
	 * handled recursively.
	 */
	public Set<Dependency> listDependencies(ModuleRegistry resolver, Set<File> sourceRoots, ClassLoader compileClassPath,
	                                        XMLInputFactory parser) {
		return listDependencies(resource, module, resolver, sourceRoots, compileClassPath, parser);
	}

	/**
	 * @param xsltFile    The XSLT resource.
	 * @param module      The module that contains the XSLT resource.
	 * @param resolver    The module registry, for resolving dependency URIs.
	 * @param sourceRoots Set of root directories containing source files. This is used to resolve
	 *                    Java source files when this method is called during compilation of the
	 *                    module.
	 * @param parser      The StAX input factory for parsing the XML.
	 */
	static Set<Dependency> listDependencies(URL xsltFile, Module module, ModuleRegistry resolver, Set<File> sourceRoots,
	                                        ClassLoader compileClassPath, XMLInputFactory parser) {
		if (cache.containsKey(xsltFile)) {
			return cache.get(xsltFile);
		} else {
			Set<Dependency> dependencies = new HashSet<>();
			try (InputStream is = xsltFile.openStream()) {
				XMLEventReader reader = parser.createXMLEventReader(is);
				try {
					int depth = 0;
					while (reader.hasNext()) {
						XMLEvent event = reader.peek();
						if (event.isStartElement()) {
							StartElement elem = event.asStartElement();
							QName elemName = elem.getName();
							if (depth == 0 && !(XSL_STYLESHEET.equals(elemName) || XSL_PACKAGE.equals(elemName))) {
								throw new IllegalArgumentException(
									"File is not XSLT: " + xsltFile + ": found root element " + elemName);
							} else if (depth == 1 && (XSL_IMPORT.equals(elemName) || XSL_INCLUDE.equals(elemName))) {
								Attribute href = elem.getAttributeByName(_HREF);
								if (href == null)
									throw new IllegalArgumentException(
										"" + xsltFile + ": Invalid XSLT: missing href attribute on " + elemName);
								URI uri = URI.create(href.getValue());
								if (uri.isAbsolute()) {
									Module m = resolver.getModuleByComponent(uri);
									if (m != null) {
										Component component = m.getComponent(uri);
										if (component == null)
											throw new IllegalStateException("coding error"); // can not happen
										dependencies.add(component);
									} else {
										throw new RuntimeException("" + xsltFile + ": Couldn't resolve href " + uri);
									}
								} else {
									// resolve relative path
									dependencies.addAll(
										listDependencies(URLs.resolve(URLs.asURI(xsltFile), uri).toURL(), module,
										                 resolver, sourceRoots, compileClassPath, parser));
								}
							} else if (depth == 1 && XSL_USE_PACKAGE.equals(elemName)) {
								Attribute name = elem.getAttributeByName(_NAME);
								if (name == null)
									throw new IllegalArgumentException(
										"" + xsltFile + ": Invalid XSLT: missing name attribute on " + elemName);
								Attribute version = elem.getAttributeByName(_PACKAGE_VERSION);
								dependencies.add(
									version != null ? new UseXSLTPackage(name.getValue(), version.getValue())
									                : new UseXSLTPackage(name.getValue()));
							}
							@SuppressWarnings("unchecked")
							Iterator<Namespace> ns = elem.getNamespaces();
							while (ns.hasNext()) {
								String nsUri = ns.next().getNamespaceURI();
								// In addition to checking that the namespace has the form of a
								// fully qualified Java class name, use the heuristic that
								// ExtensionFunctionProvider classes are not in the default package.
								if (FQCN.matcher(nsUri).matches() && nsUri.contains(".")) {
									// assuming it is the namespace of a XPath extension function, and the function
									// is used within this element
									String className = nsUri;
									// try to resolve the class
									String javaSourceFile = className.split("\\$")[0].replace('.', '/') + ".java";
									boolean internal = false; {
										if (sourceRoots != null)
											for (File root : sourceRoots)
												if (new File(root, javaSourceFile).exists()) {
													// Java source file is inside the module we are compiling
													internal = true;
													break; }}
									if (!internal) {
										// if the function is not implemented in the same module, the class must be
										// on the class path and resolvable through ModuleRegistry
										Class c; {
											try {
												c = Class.forName(className, true, compileClassPath);
											} catch (Throwable e) {
												throw new RuntimeException(
													"" + xsltFile
													+ ": Java dependency could not be resolved: class not on class path: "
													+ className);
											}
										}
										if (resolver.getModuleByClass(c) == null)
											throw new RuntimeException(
												"" + xsltFile
												+ ": Java dependency could not be resolved: no module provides class: "
												+ className);

									}
									dependencies.add(new JavaDependency(module, className));
								}
							}
							depth++;
						} else if (event.isEndElement()) {
							depth--;
						}
						reader.next();
					}
				} finally {
					reader.close();
				}
			} catch (IOException e) {
				throw new RuntimeException("Couldn't access XSLT file " + xsltFile, e);
			} catch (XMLStreamException e) {
				throw new RuntimeException("Couldn't parse XSLT file " + xsltFile, e);
			}
			cache.put(xsltFile, dependencies);
			return dependencies;
		}
	}

	private final static Map<URL,Set<Dependency>> cache = new HashMap<>();

	private final static Pattern FQCN = Pattern.compile("([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*");

}
