package org.daisy.pipeline.modules;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.daisy.pipeline.modules.Component;
import org.daisy.pipeline.modules.Entity;
import org.daisy.pipeline.modules.Module;
import org.daisy.pipeline.modules.ResourceLoader;
import org.daisy.pipeline.xmlcatalog.XmlCatalog;

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
	private final Logger mLogger = LoggerFactory.getLogger(getClass());
	
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
			Iterable<URL> entries = this.loader.loadResources(rule.getValue().toString());
			for (URL url : entries) {
				try {
					//get tail of the path i.e. ../static/css/ -> /css/
					String path = url.toURI().getPath().toString().replace(rule.getValue().toString().replace("..",""),"");
					withComponent(rule.getKey().resolve(URI.create(path)), url.toString());
				} catch (URISyntaxException e) {
					mLogger.warn("Exception while generating paths");
				}
			}
		}
		return self();
	}
}
