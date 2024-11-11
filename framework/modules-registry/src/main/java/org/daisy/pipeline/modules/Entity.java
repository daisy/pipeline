package org.daisy.pipeline.modules;

import java.net.URI;
import java.net.URL;
import java.nio.file.NoSuchFileException;

import org.daisy.common.file.URLs;

/**
 * Entity represents a public id and its path inside the module. Only handles
 * publicId as the privates one will be loaded as components.
 */
public class Entity {

	private final Module module;
	private final String publicId;
	private final URL resource;

	/**
	 * Instantiates a new entity.
	 */
	public Entity(Module module, String publicId, String path) throws NoSuchFileException {
		this.module = module;
		this.publicId = publicId;
		resource = module.getResource(path);
	}

	/**
	 * Gets the module owner of this entity.
	 */
	public Module getModule() {
		return module;
	}

	/**
	 * Gets the public id.
	 *
	 * @return the public id
	 */
	public String getPublicId() {
		return publicId;
	}

	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	public URI getResource() {
		return URLs.asURI(resource);
	}
}
