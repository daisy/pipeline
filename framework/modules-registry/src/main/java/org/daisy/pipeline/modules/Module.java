package org.daisy.pipeline.modules;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterators;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.xmlcatalog.XmlCatalog;
import org.daisy.pipeline.xmlcatalog.XmlCatalogParser;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Daisy pipeline module holds a set of components accessible via their uri, its
 * name, version and dependencies.
 */
public abstract class Module {

	private String name;
	private String version;
	private String title;
	private final Map<URI,Component> components = new HashMap<>();
	private final Map<String,Entity> entities = new HashMap<>();

	private static final Logger mLogger = LoggerFactory.getLogger(Module.class);

	/**
	 * Instantiate a new module
	 */
	protected Module(String name, String version, String title) {
		this.name = name;
		this.version = version;
		this.title = title;
	}

	/**
	 * Initialize the module
	 */
	protected void init(XmlCatalogParser parser) {
		Class<?> thisClass = this.getClass();
		try {
			URI jarFileURI = thisClass.getProtectionDomain().getCodeSource().getLocation().toURI();
			try {
				File jarFile = new File(jarFileURI);
				mLogger.trace("Creating module from JAR: " + jarFile);
				parseCatalog(
					parser,
					new ResourceLoader() {
						// Can't use ClassLoader.getResource() because there can be name
						// clashes between resources in different JARs. Alternative
						// solution would be to have a ClassLoader for each JAR.
						@Override
						public URL loadResource(String path) {
							// Paths are assumed to be relative to META-INF
							if (!path.startsWith("../")) {
								throw new RuntimeException("Paths must start with '../' but got '" + path + "'");
							}
							path = path.substring(2);
							try {
								return jarFile.isDirectory() ?
									new URL(jarFile.toURI().toASCIIString() + path) :
									new URL("jar:" + jarFile.toURI().toASCIIString() + "!" + path);
							} catch (MalformedURLException e) {
								throw new RuntimeException(e);
							}
						}
						@Override
						public Iterable<URL> loadResources(final String path) {
							throw new UnsupportedOperationException("Not supported without OSGi.");
						}
					});
			} catch (IllegalArgumentException e) {
				// Could be because we are running in OSGi context
				OSGiHelper.init(this, parser);
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	// static nested class in order to delay class loading
	private static abstract class OSGiHelper {
		static void init(Module thiz, XmlCatalogParser parser) {
			Bundle bundle = FrameworkUtil.getBundle(thiz.getClass());
			mLogger.trace("Creating module from OSGi bundle: " + bundle);
			ResourceLoader loader = new ResourceLoader() {
				@Override
				public URL loadResource(String path) {
					// Paths are assumed to be relative to META-INF
					if (!path.startsWith("../")) {
						throw new RuntimeException("Paths must start with '../' but got '" + path + "'");
					}
					path = path.substring(3);
					URL url = bundle.getResource(path);
					return url;
				}
				@Override
				public Iterable<URL> loadResources(final String path) {
					return new Iterable<URL>() {
						@Override
						public Iterator<URL> iterator() {
							return Iterators.forEnumeration(
								bundle.findEntries(path.replace("../", ""), "*", true));
						}
					};
				}
			};
			thiz.parseCatalog(parser, loader);
			// these fields are already set, but now get the values from the bundle metadata
			thiz.name = bundle.getHeaders().get("Bundle-Name").toString();
			thiz.version = bundle.getVersion().toString();
			thiz.title = bundle.getSymbolicName();
		}
	}

	/**
	 * Parse catalog.xml file
	 */
	private void parseCatalog(XmlCatalogParser parser, ResourceLoader loader) {
		URL catalogURL = loader.loadResource("../META-INF/catalog.xml");
		if (catalogURL == null)
			throw new RuntimeException("/META-INF/catalog.xml file not found");
		XmlCatalog catalog = parser.parse(URLs.asURI(catalogURL));
		parseCatalog(catalog, loader);
	}

	// package private for unit tests
	void parseCatalog(XmlCatalog catalog, ResourceLoader loader) {
		for (Map.Entry<URI, URI> entry : catalog.getSystemIdMappings().entrySet()) {
			addComponent(entry.getKey(), entry.getValue().toString(), loader);
		}
		for (Map.Entry<URI, URI> entry : catalog.getUriMappings().entrySet()) {
			addComponent(entry.getKey(), entry.getValue().toString(), loader);
		}
		for (Map.Entry<String, URI> entry : catalog.getPublicMappings().entrySet()) {
			addEntity(entry.getKey(), entry.getValue().toString(), loader);
		}
		for (Map.Entry<URI, URI> rule : catalog.getRewriteUris().entrySet()) {
			Iterable<URL> entries = loader.loadResources(rule.getValue().toString());
			for (URL url : entries) {
				try {
					// get tail of the path i.e. ../static/css/ -> /css/
					String path = url.toURI().getPath().toString().replace(rule.getValue().toString().replace("..",""),"");
					addComponent(rule.getKey().resolve(URI.create(path)), url.toString(), loader);
				} catch (URISyntaxException e) {
					mLogger.warn("Exception while generating paths");
				}
			}
		}
	}

	private void addComponent(URI uri, String path, ResourceLoader loader) {
		mLogger.trace("add component:" + uri.toString() + ", path: " + path);
		Component component = new Component(uri, path, loader);
		component.setModule(this);
		components.put(component.getURI(), component);
	}
	
	
	private void addEntity(String publicId, String path, ResourceLoader loader) {
		mLogger.trace("add entity:" + publicId.toString() + ", path: " + path);
		Entity entity = new Entity(publicId, path, loader);
		entity.setModule(this);
		entities.put(entity.getPublicId(), entity);
	}

	/**
	 * Gets the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the version.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Gets the title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Gets the components.
	 */
	public Iterable<Component> getComponents() {
		return components.values();
	}

	/**
	 * Gets the component identified by the given uri.
	 */
	public Component getComponent(URI uri) {
		return components.get(uri);
	}

	/**
	 * Gets the list of entities.
	 */
	public Iterable<Entity> getEntities() {
		return entities.values();
	}

	/**
	 * Gets the entity identified by the given public id.
	 */
	public Entity getEntity(String publicId) {
		return entities.get(publicId);
	}

	@Override
	public String toString() {
		return getName() + " [" + getVersion() + "]";
	}
}
