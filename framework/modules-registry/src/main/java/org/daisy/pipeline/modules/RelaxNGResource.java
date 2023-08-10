package org.daisy.pipeline.modules;

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
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLInputFactory;

import org.daisy.common.file.URLs;

public class RelaxNGResource {

	private static final String RNG_NS = "http://relaxng.org/ns/structure/1.0";
	static final QName RNG_GRAMMAR = new QName(RNG_NS, "grammar");
	private static final QName RNG_INCLUDE = new QName(RNG_NS, "include");
	private static final QName _HREF = new QName("href");

	private final URL resource;

	public RelaxNGResource(Module module, String path) throws NoSuchFileException {
		this.resource = module.getResource(path);
	}

	public RelaxNGResource(Component component) {
		try {
			this.resource = component.getResource().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException("coding error");
		}
	}

	/**
	 * List the dependencies of this resource.
	 *
	 * This recursively resolves all <code>include</code> that reference a component (either in the
	 * same module or in another one). Other dependencies are ignored.
	 *
	 * The dependencies are (external) {@link Component}. Included resources from the same module
	 * are handled recursively.
	 */
	public Set<Dependency> listDependencies(ModuleRegistry resolver, XMLInputFactory parser) {
		return listDependencies(resource, resolver, parser);
	}

	/**
	 * @param rngFile  The RNG resource.
	 * @param resolver The module registry, for resolving dependency URIs.
	 * @param parser   The StAX input factory for parsing the XML.
	 */
	static Set<Dependency> listDependencies(URL rngFile, ModuleRegistry resolver, XMLInputFactory parser) {
		if (cache.containsKey(rngFile)) {
			return cache.get(rngFile);
		} else {
			Set<Dependency> dependencies = new HashSet<>();
			try (InputStream is = rngFile.openStream()) {
				XMLEventReader reader = parser.createXMLEventReader(is);
				try {
					int depth = 0;
					while (reader.hasNext()) {
						XMLEvent event = reader.peek();
						if (event.isStartElement()) {
							if (depth == 0 && !RNG_GRAMMAR.equals(event.asStartElement().getName())) {
								throw new IllegalArgumentException("File is not RNG: " + rngFile);
							} else if (depth == 1 && RNG_INCLUDE.equals(event.asStartElement().getName())) {
								Attribute href = event.asStartElement().getAttributeByName(_HREF);
								if (href != null) {
									URI uri = URI.create(href.getValue());
									if (uri.isAbsolute()) {
										Module m = resolver.getModuleByComponent(uri);
										if (m != null) {
											Component component = m.getComponent(uri);
											if (component == null)
												throw new IllegalStateException("coding error"); // can not happen
											dependencies.add(component);
										} else {
											throw new RuntimeException("" + rngFile + ": Couldn't resolve href " + uri);
										}
									} else {
										// resolve relative path
										dependencies.addAll(
											listDependencies(URLs.resolve(URLs.asURI(rngFile), uri).toURL(), resolver, parser));
									}
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
				throw new RuntimeException("Couldn't access RelaxNG file " + rngFile, e);
			} catch (XMLStreamException e) {
				throw new RuntimeException("Couldn't parse RelaxNG file " + rngFile, e);
			}
			cache.put(rngFile, dependencies);
			return dependencies;
		}
	}

	private final static Map<URL,Set<Dependency>> cache = new HashMap<>();

}
