package org.daisy.dotify.common.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Provides an in-memory stream juggler.
 *
 * @author Joel Håkansson
 */
public class ByteArrayStreamJuggler implements StreamJuggler {
    private static final int BUF_SIZE = 65536;
    private InputStreamMaker ci;
    private ByteArrayOutputStream co;
    private final File output;

    /**
     * Constructs a new instance.
     *
     * @param input  An existing input file
     * @param output An output file
     * @throws IOException An IOException is thrown if the input does not exist
     *                     or if the input or output is a directory.
     */
    public ByteArrayStreamJuggler(File input, File output) throws IOException {
        if (!input.exists()) {
            throw new FileNotFoundException();
        }
        if (!input.isFile() || (output.exists() && !output.isFile())) {
            throw new IOException("Cannot perform this operation on directories.");
        }
        this.output = output;
        this.ci = new FileInputStreamMaker(input);
        this.co = new ByteArrayOutputStream(BUF_SIZE);
    }

    @Override
    public InputStreamMaker getInputStreamMaker() {
        return ci;
    }

    @Override
    public OutputStream getOutputStream() {
        return co;
    }

    @Override
    public void reset() throws IOException {
        if (ci == null || co == null) {
            throw new IllegalStateException("Cannot swap after close.");
        }
        ci = new ByteArrayInputStreamMaker(co.toByteArray());
        co = new ByteArrayOutputStream(BUF_SIZE);
    }

    @Override
    public void close() throws IOException {
        try {
            if (co.size() > 0) {
                FileIO.copy(new ByteArrayInputStream(co.toByteArray()), new FileOutputStream(output));
            } else {
                FileIO.copy(ci.newInputStream(), new FileOutputStream(output));
            }
        } finally {
            ci = null;
            co = null;
        }
    }

}
