package org.daisy.pipeline.modules;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * Daisy pipeline module holds a set of components accesible via their uri, its
 * name, version and dependencies.
 */
public class Module implements ModuleRef {

	/** The name. */
	private final String name;

	/** The version. */
	private final String version;

	/** The title. */
	private final String title;

	/** The components. */
	private final HashMap<URI, Component> components = new HashMap<URI, Component>();
	/** The entities. */
	private final HashMap<String, Entity> entities = new HashMap<String, Entity>();

	/**
	 * Instantiates a new module.
	 * 
	 * @param name
	 *            the name
	 * @param version
	 *            the version
	 * @param title
	 *            the title
	 * @param dependencies
	 *            the dependencies
	 * @param components
	 *            the components
	 * @param entities
	 *            the entities
	 */
	public Module(String name, String version, String title,
			List<Component> components, List<Entity> entities) {
		this.name = name;
		this.version = version;
		this.title = title;

		for (Component component : components) {
			component.setModule(this);
			this.components.put(component.getURI(), component);
		}

		for (Entity entity : entities) {
			entity.setModule(this);
			this.entities.put(entity.getPublicId(), entity);
		}

	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the version.
	 * 
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Gets the title.
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Gets the components.
	 * 
	 * @return the components
	 */
	public Iterable<Component> getComponents() {
		return components.values();
	}

	/**
	 * Gets the component identified by the given uri.
	 * 
	 * @param uri
	 *            the uri
	 * @return the component
	 */
	public Component getComponent(URI uri) {
		return components.get(uri);
	}

	/**
	 * Gets the list of entities.
	 * 
	 * @return the entities
	 */
	public Iterable<Entity> getEntities() {
		return entities.values();
	}

	/**
	 * Gets the entity identified by the given public id.
	 * 
	 * @param publicId
	 *            the public id
	 * @return the entity
	 */
	public Entity getEntity(String publicId) {
		return entities.get(publicId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName() + " [" + getVersion() + "]";
	}

	public Module get() {
		return this;
	}
}
