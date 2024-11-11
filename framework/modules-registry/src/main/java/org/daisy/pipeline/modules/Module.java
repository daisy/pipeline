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
import java.util.function.Supplier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;

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
	protected final Map<URI,Supplier<Component>> components = new HashMap<>();
	private final Map<String,Supplier<Entity>> entities = new HashMap<>();
	private final Map<String,XSLTPackage> xsltPackages = new HashMap<>();
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
	 * Called by {@link ModuleRegistry} to resolve dependencies of components. If a dependency of a
	 * component can not be resolved, this does not result in an exception, but the component will
	 * not be created (and consequently will not be available through the {@link #getComponent()} or
	 * {@link #getEntity} method).
	 */
	public abstract void resolveDependencies();

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
			URI path = entry.getValue();
			if (path.isAbsolute() || path.getSchemeSpecificPart().startsWith("/"))
				throw new NoSuchFileException("Expected a relative path, but got: " + path);
			module.addComponent(entry.getKey(), path.getPath());
		}
		for (Map.Entry<URI, URI> entry : catalog.getUriMappings().entrySet()) {
			URI path = entry.getValue();
			if (path.isAbsolute() || path.getSchemeSpecificPart().startsWith("/"))
				throw new NoSuchFileException("Expected a relative path, but got: " + path);
			module.addComponent(entry.getKey(), path.getPath());
		}
		for (Map.Entry<String, URI> entry : catalog.getPublicMappings().entrySet()) {
			URI path = entry.getValue();
			if (path.isAbsolute() || path.getSchemeSpecificPart().startsWith("/"))
				throw new NoSuchFileException("Expected a relative path, but got: " + path);
			module.addEntity(entry.getKey(), path.getPath());
		}
		for (Map.Entry<URI, URI> rule : catalog.getRewriteUris().entrySet()) {
			URI path = rule.getValue();
			if (path.isAbsolute() || path.getSchemeSpecificPart().startsWith("/"))
				throw new NoSuchFileException("Expected a relative path, but got: " + path);
			Iterable<URL> entries = module.loader.loadResources(path.getPath());
			for (URL url : entries) {
				try {
					// get tail of the path i.e. ../static/css/ -> /css/
					String rewritten = url.toURI().getPath().replace(path.getPath().replace("..", ""), "");
					module.addComponent(new Component(module, rule.getKey().resolve(URI.create(rewritten)), url));
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
		components.put(component.getURI(), () -> component);
		return true;
	}

	protected boolean addEntity(String publicId, String path) throws NoSuchFileException {
		getLogger().trace("Adding entity: " + publicId.toString() + ", path: " + path);
		return addEntity(new Entity(this, publicId, path));
	}

	protected boolean addEntity(Entity entity) {
		entities.put(entity.getPublicId(), () -> entity);
		return true;
	}

	protected boolean addXSLTPackage(String path, XMLInputFactory parser) throws NoSuchFileException, IllegalArgumentException {
		return addXSLTPackage(new XSLTPackage(this, path, parser));
	}

	protected boolean addXSLTPackage(String name, String version, String path) throws NoSuchFileException {
		return addXSLTPackage(new XSLTPackage(this, name, version, path));
	}

	protected boolean addXSLTPackage(XSLTPackage pack) {
		xsltPackages.put(pack.getName(), pack);
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
	 * @param path The (not URL-encoded) path, relative to catalog.xml, of a resource inside the JAR
	 *             or class directory of the module.
	 * @return An encoded absolute URL
	 * @throws NoSuchFileException if the resource at {@code path} is not available
	 */
	public URL getResource(String path) throws NoSuchFileException {
		return loader.loadResource(path);
	}

	/**
	 * Whether this module provides a component with the given URI. This assumes that the component
	 * can be resolved.
	 *
	 * If the component could not be resolved (e.g. due to a missing dependency), this will result
	 * in a {@link ResolutionException} when the {@link #getComponent} method is called.
	 */
	public boolean hasComponent(URI uri) {
		return components.containsKey(uri);
	}

	/**
	 * Gets all the component URIs.
	 */
	public Set<URI> getComponents() {
		return components.keySet();
	}

	/**
	 * Gets the component identified by the given URI.
	 *
	 * @throws NoSuchElementException if this module provides a component with the given URI.
	 * @throws ResolutionException if the component could not be resolved, e.g. due to a missing dependency.
	 */
	public Component getComponent(URI uri) throws NoSuchElementException, ResolutionException {
		Supplier<Component> s = components.get(uri);
		if (s == null)
			throw new NoSuchElementException();
		Component c = s.get();
		if (c == null)
			throw new ResolutionException();
		return c;
	}

	/**
	 * Gets the list of entity IDs.
	 */
	public Set<String> getEntities() {
		return entities.keySet();
	}

	/**
	 * Whether this module provides a entity with the given ID. This assumes that the entity can be
	 * resolved.
	 *
	 * If the entity could not be resolved (e.g. due to a missing dependency), this will result in a
	 * {@link ResolutionException} when the {@link #getEntity} method is called.
	 */
	public boolean hasEntity(String publicId) {
		return entities.containsKey(publicId);
	}

	/**
	 * Gets the entity identified by the given ID.
	 *
	 * @throws NoSuchElementException if this module provides an entity with the given ID.
	 * @throws ResolutionException if the entity could not be resolved, e.g. due to a missing dependency.
	 */
	public Entity getEntity(String publicId) throws NoSuchElementException, ResolutionException {
		Supplier<Entity> s = entities.get(publicId);
		if (s == null)
			throw new NoSuchElementException();
		Entity e = s.get();
		if (e == null)
			throw new ResolutionException();
		return e;
	}

	/**
	 * Gets the list of XSLT packages.
	 */
	public Iterable<XSLTPackage> getXSLTPackages() {
		return xsltPackages.values();
	}

	/**
	 * Gets the XSLT package identified by the given name.
	 */
	public XSLTPackage getXSLTPackage(String name) {
		return xsltPackages.get(name);
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
