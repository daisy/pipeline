package org.daisy.dotify.common.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Provides a in-memory input stream maker.
 *
 * @author Joel Håkansson
 */
public class ByteArrayInputStreamMaker implements InputStreamMaker {
    private final byte[] buf;

    /**
     * Creates a new byte array input stream maker with the specified data.
     *
     * @param buf the data
     */
    public ByteArrayInputStreamMaker(byte[] buf) {
        this.buf = buf;
    }

    @Override
    public InputStream newInputStream() throws IOException {
        return new ByteArrayInputStream(buf);
    }

}
