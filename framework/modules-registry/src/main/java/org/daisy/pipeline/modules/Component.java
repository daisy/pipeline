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



	/** The uri. */
	private final URI uri;

	/** The path. */
	private final String path;
	// private Space space;
	/** The loader. */
	private final ResourceLoader loader;

	/** The module. */
	private Module module;

	/** The m logger. */
	Logger mLogger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * Instantiates a new component.
	 *
	 * @param uri
	 *            the uri
	 * @param path
	 *            the path
	 * @param loader
	 *            the loader which actually able to load the resource.
	 */
	public Component(URI uri, String path, ResourceLoader loader) {
		this.uri = uri;
		this.path = path;
		this.loader = loader;
	}

	/**
	 * Gets the component's URI.
	 *
	 * @return the uRI
	 */
	public URI getURI() {
		return uri;
	}

	/*
	 * public Space getSpace() { return space; }
	 */

	/**
	 * Gets the resource's real uri.
	 *
	 * @return the resource
	 */
	public URI getResource() {
		try {

			mLogger.trace("Getting resource from component " + this + ": " + path);
			URL url= loader.loadResource(path);
			if(url!=null) {
				return URLs.asURI(url);
			} else {
				return null;
			}

		} catch (Exception e) {
			mLogger.debug("Resource " + path + " does not exist", e);
			return null;
		}
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

	/**
	 * Gets the module owner of this component.
	 *
	 * @return the module
	 */
	public Module getModule() {
		return module;
	}

	/**
	 * Sets the module.
	 *
	 * @param module
	 *            the new module
	 */
	public void setModule(Module module) {
		this.module = module;
	}
}
