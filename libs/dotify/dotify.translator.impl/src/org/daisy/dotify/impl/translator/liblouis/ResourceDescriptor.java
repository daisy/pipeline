package org.daisy.dotify.impl.translator.liblouis;

import java.io.InputStream;
import java.nio.charset.Charset;

class ResourceDescriptor {
	private final InputStream inputStream;
	private final Charset encoding;

	public ResourceDescriptor(InputStream inputStream, Charset encoding) {
		super();
		this.inputStream = inputStream;
		this.encoding = encoding;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public Charset getEncoding() {
		return encoding;
	}
}
