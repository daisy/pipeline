package org.daisy.pipeline.modules;

import java.net.URI;
import java.net.URL;
import java.nio.file.NoSuchFileException;

import org.daisy.common.file.URLs;

/**
 * Module component now based on expath package components.
 */
public class Component implements Dependency {

	private final Module module;
	private final URI uri;
	private final URL resource;
	private final String version;

	/**
	 * Instantiates a new component.
	 */
	public Component(Module module, URI uri, String path) throws NoSuchFileException {
		this.module = module;
		this.uri = uri;
		resource = module.getResource(path);
		version = module.getVersion().replaceAll("-SNAPSHOT$", "");
	}

	public Component(Module module, URI uri, URL resource) {
		this.module = module;
		this.uri = uri;
		this.resource = resource;
		version = module.getVersion().replaceAll("-SNAPSHOT$", "");
	}

	/**
	 * Gets the component's URI.
	 *
	 * @return the uRI
	 */
	public URI getURI() {
		return uri;
	}

	/**
	 * Gets the resource's real uri.
	 *
	 * @return the resource
	 */
	public URI getResource() {
		return URLs.asURI(resource);
	}

	/**
	 * Get the version of this component.
	 *
	 * Defaults to the version number of the module. Method should be overridden by components that
	 * have their own versioning.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Gets the module owner of this component.
	 */
	public Module getModule() {
		return module;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + uri + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (!(o instanceof Component))
			return false;
		Component that = (Component)o;
		if (!module.equals(that.module))
			return false;
		if (!uri.equals(that.uri))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + module.hashCode();
		result = prime * result + uri.hashCode();
		return result;
	}
}
