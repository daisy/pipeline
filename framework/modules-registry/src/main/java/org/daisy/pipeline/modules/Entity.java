package org.daisy.pipeline.modules;

import java.net.URI;
import java.net.URL;

import org.daisy.common.file.URLs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entity represents a public id and its path inside the module. Only handles
 * publicId as the privates one will be loaded as components.
 */
public class Entity {

	private final Module mModule;
	private final String mPublicId;
	private final String mPath;

	private static Logger mLogger = LoggerFactory.getLogger(Entity.class);

	/**
	 * Instantiates a new entity.
	 */
	public Entity(Module module, String publicId, String path) {
		mModule = module;
		mPublicId = publicId;
		mPath = path;
	}

	/**
	 * Gets the module owner of this entity.
	 */
	public Module getModule() {
		return mModule;
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
			URL url = mModule.loader.loadResource(mPath);
			if (url != null) {
				return URLs.asURI(url);
			} else {
				return null;
			}
		} catch (Exception e) {
			mLogger.debug("Resource " + mPath + " does not exist", e);
			return null;
		}
	}
}
