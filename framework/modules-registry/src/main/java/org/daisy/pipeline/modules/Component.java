package org.daisy.pipeline.modules;

import java.net.URI;
import java.net.URL;

import org.daisy.common.file.URLs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module component now based on expath package components.
 */
public class Component {

	private final Module module;
	private final URI uri;
	private final String path;
	private final String version;

	Logger mLogger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * Instantiates a new component.
	 */
	public Component(Module module, URI uri, String path) {
		this.module = module;
		this.uri = uri;
		this.path = path;
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
		try {
			mLogger.trace("Getting resource from component " + this + ": " + path);
			URL url = module.loader.loadResource(path);
			return url != null ? URLs.asURI(url) : null;
		} catch (Exception e) {
			mLogger.debug("Resource " + path + " does not exist", e);
			return null;
		}
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
}
