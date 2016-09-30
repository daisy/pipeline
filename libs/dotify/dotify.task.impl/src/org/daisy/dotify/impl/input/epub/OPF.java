package org.daisy.dotify.impl.input.epub;

import java.util.List;
import java.util.Map;

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

	public String getPath() {
		return path;
	}

	public List<String> getSpine() throws EPUB3ReaderException {
		return spine;
	}

	public Map<String, String> getManifest() {
		return manifest;
	}
}
