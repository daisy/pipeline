package org.daisy.pipeline.modules;

import java.net.URI;
import java.net.URL;

import org.daisy.common.file.URIs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// TODO: Auto-generated Javadoc
/**
 * Entity represents a public id and its path inside the module. Only handles
 * publicId as the privates one will be loaded as components.
 */
public class Entity {

	/** The Public id. */
	private final String mPublicId;

	/** The Path. */
	private final String mPath;

		/** The loader. */
	private final ResourceLoader mLoader;

	/** The module. */
	private Module mModule;

	private static Logger mLogger = LoggerFactory.getLogger(Entity.class);
	/**
	 * Instantiates a new entity.
	 *
	 * @param publicId the public id
	 * @param path the path
	 * @param loader the loader
	 */
	public Entity(String publicId, String path, ResourceLoader loader) {
		super();
		mPublicId = publicId;
		mPath = path;
		mLoader = loader;
	}

	/**
	 * Gets the module.
	 *
	 * @return the module
	 */
	public Module getModule() {
		return mModule;
	}

	/**
	 * Sets the module.
	 *
	 * @param module the new module
	 */
	public void setModule(Module module) {
		mModule = module;
	}

	/**
	 * Gets the public id.
	 *
	 * @return the public id
	 */
	public String getPublicId() {
		return mPublicId;
	}

	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	public URI getResource() {
try {

			mLogger.trace("Getting resource from entity " + this + ": " + mPath);
			URL url= mLoader.loadResource(mPath);
			if(url!=null) {
				return URIs.asURI(url);
			} else {
				return null;
			}

		} catch (Exception e) {
			mLogger.debug("Resource " + mPath + " does not exist", e);
			return null;
		}
	}

	/**
	 * Gets the loader.
	 *
	 * @return the loader
	 */
	public ResourceLoader getLoader() {
		return mLoader;
	}


}
