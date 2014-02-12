package se.mtm.common.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ByteArrayInputStreamMaker implements InputStreamMaker {
	private final byte[] buf;

	public ByteArrayInputStreamMaker(byte[] buf) {
		this.buf = buf;
	}

	public InputStream newInputStream() throws IOException {
		return new ByteArrayInputStream(buf);
	}

}
