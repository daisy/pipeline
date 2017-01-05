package org.daisy.dotify.impl.input.epub;

import java.util.List;
import java.util.Map;

/**
 * Provides a data object for an OPF-file.
 * @author Joel Håkansson
 */
public class OPF {
	private final String path;
	private final List<String> spine;
	private final Map<String, String> manifest;

	OPF(String path, List<String> paths, Map<String, String> manifest) {
		super();
		this.path = path;
		this.spine = paths;
		this.manifest = manifest;
	}

	/**
	 * Gets the path of this OPF relative to the root of the epub publication.
	 * @return returns the relative path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Gets a list of spine idrefs.
	 * @return returns a list of spine idrefs
	 */
	public List<String> getSpine() {
		return spine;
	}

	/**
	 * Gets the manifest ids and paths.
	 * @return returns the manifest
	 */
	public Map<String, String> getManifest() {
		return manifest;
	}
}
