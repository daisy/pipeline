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
	ResourceLoader loader; // used in Component and Entity

	private static final Logger mLogger = LoggerFactory.getLogger(Module.class);

	/**
	 * Instantiate a new module
	 */
	protected Module(String name, String version, String title) {
		if (OSGiHelper.inOSGiContext())
			OSGiHelper.populate(this);
		else {
			this.name = name;
			this.version = version;
			this.title = title;
			try {
				URI jarFileURI = getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
				if (!jarFileURI.toString().startsWith("file:"))
					throw new RuntimeException("unexpected code source location: " + jarFileURI);
				File jarFile = new File(jarFileURI);
				getLogger().trace("Creating module from JAR: " + jarFile);
				this.loader = new ResourceLoader() {
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
					};
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}
	}

	protected Module(String name, String version, String title, ResourceLoader loader) {
		if (OSGiHelper.inOSGiContext())
			throw new RuntimeException("Constructor not supported in OSGi");
		this.name = name;
		this.version = version;
		this.title = title;
		this.loader = loader;
	}

	protected Logger getLogger() {
		return mLogger;
	}

	// static nested class in order to delay class loading
	private static abstract class OSGiHelper {

		static boolean inOSGiContext() {
			try {
				return FrameworkUtil.getBundle(OSGiHelper.class) != null;
			} catch (NoClassDefFoundError e) {
				return false;
			}
		}

		static void populate(Module thiz) {
			Bundle bundle = FrameworkUtil.getBundle(thiz.getClass());
			thiz.getLogger().trace("Creating module from OSGi bundle: " + bundle);
			thiz.loader = new ResourceLoader() {
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
			// these fields are already set, but now get the values from the bundle metadata
			thiz.name = bundle.getHeaders().get("Bundle-Name").toString();
			thiz.version = bundle.getVersion().toString();
			thiz.title = bundle.getSymbolicName();
		}
	}

	/**
	 * Parse catalog.xml file
	 */
	public static void parseCatalog(Module module, XmlCatalogParser parser) {
		URL catalogURL = module.loader.loadResource("../META-INF/catalog.xml");
		if (catalogURL == null)
			throw new RuntimeException("/META-INF/catalog.xml file not found");
		XmlCatalog catalog = parser.parse(URLs.asURI(catalogURL));
		parseCatalog(module, catalog);
	}

	public static void parseCatalog(Module module, XmlCatalog catalog) {
		for (Map.Entry<URI, URI> entry : catalog.getSystemIdMappings().entrySet()) {
			module.addComponent(entry.getKey(), entry.getValue().toString());
		}
		for (Map.Entry<URI, URI> entry : catalog.getUriMappings().entrySet()) {
			module.addComponent(entry.getKey(), entry.getValue().toString());
		}
		for (Map.Entry<String, URI> entry : catalog.getPublicMappings().entrySet()) {
			module.addEntity(entry.getKey(), entry.getValue().toString());
		}
		for (Map.Entry<URI, URI> rule : catalog.getRewriteUris().entrySet()) {
			Iterable<URL> entries = module.loader.loadResources(rule.getValue().toString());
			for (URL url : entries) {
				try {
					// get tail of the path i.e. ../static/css/ -> /css/
					String path = url.toURI().getPath().toString().replace(rule.getValue().toString().replace("..",""),"");
					module.addComponent(rule.getKey().resolve(URI.create(path)), url.toString());
				} catch (URISyntaxException e) {
					module.getLogger().warn("Exception while generating paths");
				}
			}
		}
	}

	/**
	 * This method can be overridden to exclude certain components that have unmet dependencies.
	 */
	protected boolean addComponent(URI uri, String path) {
		getLogger().trace("Adding component: " + uri.toString() + ", path: " + path);
		return addComponent(new Component(this, uri, path));
	}

	protected boolean addComponent(Component component) {
		components.put(component.getURI(), component);
		return true;
	}

	protected boolean addEntity(String publicId, String path) {
		getLogger().trace("Adding entity: " + publicId.toString() + ", path: " + path);
		return addEntity(new Entity(this, publicId, path));
	}

	protected boolean addEntity(Entity entity) {
		entities.put(entity.getPublicId(), entity);
		return true;
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
