package org.daisy.dotify.common.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Provides a file based input stream maker.
 *
 * @author Joel Håkansson
 */
public class FileInputStreamMaker implements InputStreamMaker {
    private final File f;

    /**
     * Creates a new input stream maker with the specified file as source.
     *
     * @param f the file containing the stream source
     */
    public FileInputStreamMaker(File f) {
        this.f = f;
    }

    @Override
    public InputStream newInputStream() throws IOException {
        return new FileInputStream(f);
    }

}
