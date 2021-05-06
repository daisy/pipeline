package org.daisy.dotify.common.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides file IO tools.
 *
 * @author Joel Håkansson
 */
public class FileIO {
    private static final Logger logger = Logger.getLogger(FileIO.class.getCanonicalName());

    private FileIO() {
    }

    /**
     * Copies an input stream to an output stream. In Java 9 or later, use
     * <code>is.transferTo(OutputStream)</code>.
     *
     * @param is the input stream
     * @param os the output stream
     * @throws IOException if IO fails
     */
    //TODO: @deprecated use is.transferTo(OutputStream) (since Java 9)
    //TODO: @Deprecated
    public static void copy(InputStream is, OutputStream os) throws IOException {
        try (
                InputStream bis = new BufferedInputStream(is);
                OutputStream bos = new BufferedOutputStream(os);
        ) {
            int b;
            while ((b = bis.read()) != -1) {
                bos.write(b);
            }
            bos.flush();
        }
    }

    /**
     * Creates a temp file.
     *
     * @return returns a the created file
     * @throws IOException if IO fails
     */
    public static File createTempFile() throws IOException {
        File ret = File.createTempFile("temp", null, null);
        ret.deleteOnExit();
        return ret;
    }

    /**
     * Creates a temp folder.
     *
     * @return returns the created folder
     * @throws IOException if IO fails
     */
    public static File createTempDir() throws IOException {
        File temp = File.createTempFile("temp", Long.toString(System.nanoTime()));
        if (!temp.delete()) {
            temp.deleteOnExit();
        }
        File tempDir = temp.getParentFile();
        File f;
        int i = 0;
        do {
            f = new File(tempDir, Long.toString(System.nanoTime()));
            Logger.getLogger(FileIO.class.getCanonicalName()).fine("Attempt to create dir: " + f);
            i++;
        } while (!f.mkdir() && i < 20);
        if (!f.isDirectory()) {
            throw new IOException("Failed to create temp dir.");
        }
        Logger.getLogger(FileIO.class.getCanonicalName()).info("Temp dir created: " + f);

        return f;
    }

    /**
     * Copies a folder recursively.
     *
     * @param f   the source folder
     * @param out the destination folder
     */
    public static void copyRecursive(File f, File out) {
        if (f.isDirectory()) {
            out.mkdirs();
            for (File f1 : f.listFiles()) {
                copyRecursive(f1, new File(out, f1.getName()));
            }
        } else {
            try {
                Files.copy(f.toPath(), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.throwing(FileIO.class.getCanonicalName(), "copyRecursive", e);
            }
        }
    }

    /**
     * Deletes a folder recursively.
     *
     * @param f the folder to delete
     */
    public static void deleteRecursive(File f) {
        if (f.isDirectory()) {
            for (File f1 : f.listFiles()) {
                deleteRecursive(f1);
            }
        }
        if (!f.delete()) {
            f.deleteOnExit();
        }
    }

    /**
     * Compares the input streams binary.
     *
     * @param f1 the first input stream
     * @param f2 the second input stream
     * @return returns -1 if the streams are equal, byte position where first
     * difference occurred otherwise
     * @throws IOException if IO fails
     */
    public static long diff(InputStream f1, InputStream f2) throws IOException {
        try (
                InputStream bf1 = new BufferedInputStream(f1);
                InputStream bf2 = new BufferedInputStream(f2)
        ) {
            int b1;
            int b2;
            long pos = 0;
            while ((b1 = bf1.read()) != -1 & b1 == (b2 = bf2.read())) {
                pos++;
                // continue
            }
            if (b1 != -1 || b2 != -1) {
                return pos;
            }
            return -1;
        }
    }

    /**
     * Lists files in the specified folder and sub folders having the specified extension.
     * For non-recursive file listing, use: <code>dir.listFiles((parent, name)-&gt;name.endsWith(ext));</code>
     *
     * @param dir the folder to start search
     * @param ext the file extensions to find
     * @return returns a list of files
     */
    public static Collection<File> listFilesRecursive(File dir, String ext) {
        List<File> files = new ArrayList<>();
        listFilesRecursive(files, dir, pathname -> pathname.isDirectory() || pathname.getName().endsWith(ext));
        return files;
    }

    private static void listFilesRecursive(List<File> files, File dir, FileFilter ff) {
        File[] listFiles = dir.listFiles(ff);
        if (listFiles == null) {
            return;
        }
        for (File f : listFiles) {
            if (f.isDirectory()) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Scanning dir " + f);
                }
                listFilesRecursive(files, f, ff);
            } else if (f.isFile()) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Adding file: " + f);
                }
                files.add(f);
            } else {
                // ignore
            }
        }
    }

    /**
     * Converts an array of File objects into URL's.
     *
     * @param files the files to convert
     * @return returns an array of URL's
     */
    public static URL[] toURL(File[] files) {
        List<URL> urls = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (File f : files) {
                try {
                    urls.add(f.toURI().toURL());
                } catch (MalformedURLException e) {
                    logger.warning("Failed to convert " + f + " into an URL.");
                }
            }
        }
        return urls.toArray(new URL[]{});
    }
}
