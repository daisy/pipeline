package org.daisy.pipeline.modules;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.daisy.common.file.URIs;

import org.daisy.pipeline.modules.Component;
import org.daisy.pipeline.modules.Entity;
import org.daisy.pipeline.modules.Module;
import org.daisy.pipeline.modules.ResourceLoader;
import org.daisy.pipeline.xmlcatalog.XmlCatalog;
import org.daisy.pipeline.xmlcatalog.XmlCatalogParser;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractModuleBuilder<T extends AbstractModuleBuilder> {
	
	protected abstract T self();
	
	private String name;
	private String version;
	private String title;
	private ResourceLoader loader;
	private final List<Component> components = new ArrayList<Component>();
	private final List<Entity> entities = new ArrayList<Entity>();

	private static final Logger mLogger = LoggerFactory.getLogger(AbstractModuleBuilder.class);
	
	public Module build() {
		return new Module(name, version, title, components, entities);
	}
	
	public T withName(String name) {
		this.name = name;
		return self();
	}
	
	public T withLoader(ResourceLoader loader) {
		this.loader = loader;
		return self();
	}
	
	public T withVersion(String version) {
		this.version = version;
		return self();
	}
	
	public T withTitle(String title) {
		this.title = title;
		return self();
	}
	
	public T withComponents(Collection<? extends Component> components) {
		this.components.addAll(components);
		return self();
	}
	
	public T withComponent(URI uri, String path) {
		mLogger.trace("withComponent:" + uri.toString() + ", path: " + path);
		components.add(new Component(uri, path, loader));
		return self();
	}
	
	public T withEntities(Collection<? extends Entity> entities) {
		this.entities.addAll(entities);
		return self();
	}
	
	public T withEntity(String publicId, String path) {
		mLogger.trace("withEntity:" + publicId.toString() + ", path: " + path);
		entities.add(new Entity(publicId, path, loader));
		return self();
	}
	
	public T withCatalog(XmlCatalog catalog) {
		for (Map.Entry<URI, URI> entry : catalog.getSystemIdMappings().entrySet()) {
			withComponent(entry.getKey(), entry.getValue().toString());
		}
		for (Map.Entry<URI, URI> entry : catalog.getUriMappings().entrySet()) {
			withComponent(entry.getKey(), entry.getValue().toString());
		}
		for (Map.Entry<String, URI> entry : catalog.getPublicMappings().entrySet()) {
			withEntity(entry.getKey(), entry.getValue().toString());
		}
		for (Map.Entry<URI, URI> rule : catalog.getRewriteUris().entrySet()) {
			try {
				String base = rule.getValue().toString();
				base = base.replaceAll("\\\\", "/");
				if(base.startsWith("..")) base = base.substring(2);
				URL baseURL = this.loader.loadResource(rule.getValue().toString());
				
				String basePath = baseURL.toString();
				// remove every last / if there is one
				while(basePath.endsWith("/"))
					basePath = basePath.substring(0, basePath.length() - 1);
				
				Iterable<URL> entries = this.loader.loadResources(rule.getValue().toString());
				for (URL url : entries) {
					// reconstruct the relative paths from both URLs
					String resourcePath = url.toString();
					String relativePath = resourcePath.substring(basePath.length() - base.length() + 1 );
					if(relativePath.startsWith("/")) relativePath = relativePath.substring(1);
					
					String relativeSubPath = resourcePath.substring(basePath.length());
					if(relativeSubPath.startsWith("/")) relativeSubPath = relativeSubPath.substring(1);
					withComponent(
							rule.getKey().resolve(URI.create(relativeSubPath)), 
							"../" + relativePath);
					
				}
			} catch (Exception e) {
				mLogger.warn("RewriteURI for " + rule.getValue().toString() + " ignored - Exception raised : " + e.getLocalizedMessage());
				
			}
		}
		return self();
	}
	
	/*
	 * Use loader to find catalog.xml file
	 */
	public T withCatalogParser(XmlCatalogParser parser) {
		if (loader == null)
			throw new UnsupportedOperationException("Resource loader not set");
		URL catalogURL = loader.loadResource("../META-INF/catalog.xml");
		if (catalogURL == null)
			throw new RuntimeException("/META-INF/catalog.xml file not found");
		return withCatalog(parser.parse(URIs.asURI(catalogURL)));
	}
	
	/*
	 * Return either a JarModuleBuilder or a OSGIModuleBuilder
	 */
	public static AbstractModuleBuilder fromContainedClass(Class<?> clazz) {
		try {
			URI jarFileURI = clazz.getProtectionDomain().getCodeSource().getLocation().toURI();
			try {
				File jarFile = new File(jarFileURI);
				mLogger.trace("Creating module from JAR: " + jarFile);
				return new JarModuleBuilder().withJarFile(jarFile);
			} catch (IllegalArgumentException e) {
				// Could be because we are running in OSGi context
				return OSGiHelper.getOSGiModuleBuilder(clazz);
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	// static nested class in order to delay class loading
	private static abstract class OSGiHelper {
		static AbstractModuleBuilder getOSGiModuleBuilder(Class<?> clazz) {
			Bundle bundle = FrameworkUtil.getBundle(clazz);
			mLogger.trace("Creating module from OSGi bundle: " + bundle);
			return new OSGIModuleBuilder().withBundle(bundle);
		}
	}
}
