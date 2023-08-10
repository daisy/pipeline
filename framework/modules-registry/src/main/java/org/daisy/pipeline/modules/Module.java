package org.daisy.pipeline.modules;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
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
	private ResourceLoader loader;

	private static final Logger mLogger = LoggerFactory.getLogger(Module.class);

	private static final Map<String,Object> fsEnv = Collections.<String,Object>emptyMap();

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
				if (!jarFile.exists())
					throw new RuntimeException("coding error");
				getLogger().trace("Creating module from JAR: " + jarFile);
				this.loader = new ResourceLoader() {
						// Can't use ClassLoader.getResource() because there can be name
						// clashes between resources in different JARs. Alternative
						// solution would be to have a ClassLoader for each JAR.
						@Override
						public URL loadResource(String path) throws NoSuchFileException {
							// Paths are assumed to be relative to META-INF
							if (!path.startsWith("../")) {
								throw new RuntimeException("Paths must start with '../' but got '" + path + "'");
							}
							path = path.substring(3);
							if (jarFile.isDirectory()) {
								File f = new File(jarFile, path);
								if (!f.exists())
									throw new NoSuchFileException("file does not exist: " + f);
								return URLs.asURL(f);
							} else {
								FileSystem fs; {
									try {
										fs = FileSystems.newFileSystem(URLs.asURI("jar:" + jarFileURI), fsEnv); }
									catch (IOException e) {
										throw new RuntimeException(e); }}
								try {
									Path f = fs.getPath("/" + path);
									if (!Files.exists(f))
										throw new NoSuchFileException("file does not exist: " + f);
									try {
										return new URL("jar:" + jarFileURI + "!/" + path); }
									catch (MalformedURLException e) {
										throw new RuntimeException(e); }}
								finally {
									try {
										fs.close(); }
									catch (IOException e) {
										throw new RuntimeException(e); }}
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
		URL catalogURL; {
			try {
				catalogURL = module.loader.loadResource("../META-INF/catalog.xml");
			} catch (NoSuchFileException e) {
				throw new RuntimeException("/META-INF/catalog.xml file not found", e);
			}
		}
		XmlCatalog catalog = parser.parse(URLs.asURI(catalogURL));
		try {
			parseCatalog(module, catalog);
		} catch (Throwable e) {
			throw new RuntimeException("catalog.xml can not be parsed", e);
		}
	}

	public static void parseCatalog(Module module, XmlCatalog catalog) throws NoSuchFileException {
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
	protected boolean addComponent(URI uri, String path) throws NoSuchFileException {
		getLogger().trace("Adding component: " + uri.toString() + ", path: " + path);
		return addComponent(new Component(this, uri, path));
	}

	protected boolean addComponent(Component component) {
		components.put(component.getURI(), component);
		return true;
	}

	protected boolean addEntity(String publicId, String path) throws NoSuchFileException {
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
	 * Get the specified resource from the module. This includes all resources, also the ones that
	 * are not exposed as components. The method can be overridden to exclude certain resources that
	 * have unmet dependencies.
	 *
	 * @param path The (not URL-encoded) path of a resource inside the JAR or class directory of the module.
	 * @return An encoded absolute URL
	 * @throws NoSuchFileException if the resource at {@code path} is not available
	 */
	public URL getResource(String path) throws NoSuchFileException {
		return loader.loadResource(path);
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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (!(o instanceof Module))
			return false;
		Module that = (Module)o;
		if (!name.equals(that.name))
			return false;
		if (!version.equals(that.version))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + name.hashCode();
		result = prime * result + version.hashCode();
		return result;
	}
}
