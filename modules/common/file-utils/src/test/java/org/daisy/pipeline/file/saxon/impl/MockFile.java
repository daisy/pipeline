package org.daisy.pipeline.file.saxon.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@SuppressWarnings("serial")
class MockFile extends File {

	private String pathname = null;
	public boolean exists = true;

	public MockFile(String pathname) {
		super(pathname);
		this.pathname = pathname;
	}

	@Override
	public String getCanonicalPath() throws IOException {
		String path = this.pathname;
		path = path.replaceAll("DOCUME~1", "Documents and Settings");
		return path;
	}

	@Override
	public File getCanonicalFile() throws IOException {
		return new MockFile(getCanonicalPath());
	}

	@Override
	public URI toURI() {
		String uri = pathname;
		if (uri.startsWith("C:\\")) {
			uri = "file:///"+uri;
		}
		uri = uri.replaceAll("\\\\", "/");
		uri = uri.replaceAll(" ", "%20");
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean exists() {
		return exists;
	}

}
