package org.daisy.dotify.common.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileInputStreamMaker implements InputStreamMaker {
	private final File f;

	public FileInputStreamMaker(File f) {
		this.f = f;
	}

	@Override
	public InputStream newInputStream() throws IOException {
		return new FileInputStream(f);
	}

}
