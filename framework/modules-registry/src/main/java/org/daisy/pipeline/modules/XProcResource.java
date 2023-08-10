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
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLInputFactory;

import org.daisy.common.file.URLs;

public class XProcResource {

	private static final String XPROC_NS = "http://www.w3.org/ns/xproc";
	private static final String CALABASH_EX_NS = "http://xmlcalabash.com/ns/extensions";
	private static final QName P_LIBRARY = new QName(XPROC_NS, "library");
	private static final QName P_DECLARE_STEP = new QName(XPROC_NS, "declare-step");
	private static final QName P_PIPELINE = new QName(XPROC_NS, "pipeline");
	private static final QName P_IMPORT = new QName(XPROC_NS, "import");
	private static final QName P_DOCUMENT = new QName(XPROC_NS, "document");
	private static final QName CX_IMPORT = new QName(CALABASH_EX_NS, "import");
	private static final QName _HREF = new QName("href");
	private static final QName _TYPE = new QName("type");

	private final URL resource;
	private final Module module;

	public XProcResource(Module module, String path) throws NoSuchFileException {
		this.resource = module.getResource(path);
		this.module = module;
	}

	public XProcResource(Component component) {
		try {
			this.resource = component.getResource().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException("coding error");
		}
		this.module = component.getModule();
	}

	/**
	 * List the dependencies of this resource.
	 *
	 * This recursively resolves all <code>p:import</code>, <code>cx:import</code>,
	 * <code>p:document</code>, <code>xsl:import</code>, <code>xsl:include</code> and
	 * <code>rng:include</code> that reference a component (either in the same module or in another
	 * one). It also resolves <code>xsl:use-package</code> and it resolves XPath extension functions
	 * defined in Java. Other dependencies are ignored.
	 *
	 * The dependencies may include (external) {@link Component}, {@link UseXSLTPackage} and
	 * (internal) {@link JavaDependency}. XSLT/XProc/RelaxNG resources from the same module are
	 * handled recursively.
	 */
	public Set<Dependency> listDependencies(ModuleRegistry resolver, Set<File> sourceRoots, XMLInputFactory parser) {
		return listDependencies(resource, true, false, module, resolver, sourceRoots, parser);
	}

	/**
	 * @param file        The resource.
	 * @param ensureXProc Assert that the resource is XProc.
	 * @param ensureXSLT  Assert that the resource is XSLT.
	 * @param module      The module that contains the resource.
	 * @param resolver    The module registry, for resolving dependency URIs.
	 * @param sourceRoots Set of root directories containing source files. This is used to resolve
	 *                    Java source files when this method is called during compilation of the
	 *                    module.
	 * @param parser      The StAX input factory for parsing the XML.
	 */
	private static Set<Dependency> listDependencies(URL file, boolean ensureXProc, boolean ensureXSLT, Module module,
	                                                ModuleRegistry resolver, Set<File> sourceRoots, XMLInputFactory parser) {
		if (cache.containsKey(file)) {
			return cache.get(file);
		} else {
			Set<Dependency> dependencies = new HashSet<>();
			try (InputStream is = file.openStream()) {
				XMLEventReader reader = parser.createXMLEventReader(is);
				try {
					int depth = 0;
					while (reader.hasNext()) {
						XMLEvent event = reader.peek();
						if (event.isStartElement()) {
							StartElement elem = event.asStartElement();
							QName name = elem.getName();
							if (depth == 0) {
								if (!(P_LIBRARY.equals(name) || P_DECLARE_STEP.equals(name) || P_PIPELINE.equals(name))) {
									if (ensureXProc)
										throw new IllegalArgumentException(
											"File is not XProc: " + file + ": found root element " + name);
									else if (XSLTResource.XSL_STYLESHEET.equals(name) || XSLTResource.XSL_PACKAGE.equals(name))
										return XSLTResource.listDependencies(file, module, resolver, sourceRoots, parser);
									else if (ensureXSLT)
										throw new IllegalArgumentException(
											"File is not XSLT: " + file + ": found root element " + name);
									else if (RelaxNGResource.RNG_GRAMMAR.equals(name))
										return RelaxNGResource.listDependencies(file, resolver, parser);
									else
										break;
								}
							} else if ((P_IMPORT.equals(name) ||
							            (CX_IMPORT.equals(name)
							             && "type='application/xml+xslt'".equals("" + elem.getAttributeByName(_TYPE)))) ||
							           P_DOCUMENT.equals(name)) {
								Attribute href = elem.getAttributeByName(_HREF);
								if (href == null)
									throw new IllegalArgumentException(
										"" + file + ": Invalid XProc: missing href attribute on " + name);
								URI uri = URI.create(href.getValue());
								if (P_IMPORT.equals(name)
								    && "http://xmlcalabash.com/extension/steps/library-1.0.xpl".equals(uri.toString()))
									; // FIXME: create dependency on XMLCalabash
								else if (uri.isAbsolute()) {
									Module m = resolver.getModuleByComponent(uri);
									if (m != null) {
										Component component = m.getComponent(uri);
										if (component == null)
											throw new IllegalStateException("coding error"); // can not happen
										dependencies.add(component);
									} else {
										throw new RuntimeException("" + file + ": Couldn't resolve href " + uri);
									}
								} else {
									// resolve relative path
									dependencies.addAll(
										listDependencies(URLs.resolve(URLs.asURI(file), uri).toURL(), P_IMPORT.equals(name),
										                 CX_IMPORT.equals(name), module, resolver, sourceRoots, parser));
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
				throw new RuntimeException("Couldn't access "
				                           + (ensureXProc ? "XProc " : ensureXSLT ? "XSLT " : "") + "file " + file, e);
			} catch (XMLStreamException e) {
				throw new RuntimeException("Couldn't parse "
				                           + (ensureXProc ? "XProc " : ensureXSLT ? "XSLT " : "") + "file " + file, e);
			}
			cache.put(file, dependencies);
			return dependencies;
		}
	}

	private final static Map<URL,Set<Dependency>> cache = new HashMap<>();

}
