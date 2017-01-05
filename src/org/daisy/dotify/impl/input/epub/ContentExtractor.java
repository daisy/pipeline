package org.daisy.dotify.impl.input.epub;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Provides unpacking of epub 3 files. 
 * @author Joel Håkansson
 *
 */
public class ContentExtractor {
	private static final Logger logger = Logger.getLogger(ContentExtractor.class.getCanonicalName());
	private final ZipInputStream zis;
	private final File output;

	private ContentExtractor(InputStream input, File output) throws EPUB3ReaderException {
		this.output = output;
		if (!output.isDirectory()) {
			throw new EPUB3ReaderException("Path must be a directory.");
		}
		this.zis = new ZipInputStream(input);
	}

	/**
	 * Unpacks the input stream and puts it in the output folder.
	 * @param input the input stream
	 * @param output the output folder
	 * @throws EPUB3ReaderException if unpacking fails
	 */
	public static void unpack(InputStream input, File output) throws EPUB3ReaderException {
		new ContentExtractor(input, output).unpack();
	}
	
	private void unpack() throws EPUB3ReaderException {
		try {
			ZipEntry entry;
			try {
				boolean mimetypeChecked = false;
				while ((entry = zis.getNextEntry()) != null) {
					if ("mimetype".equals(entry.getName())) {
						processMimetype(entry);
						mimetypeChecked = true;
					} else {
						// "mimetype" must be the first item
						// see 3.3 OCF ZIP Container Media Type Identification
						if (!mimetypeChecked) {
							Logger.getLogger(this.getClass().getCanonicalName()).warning("The first item in an epub should be a mimetype file.");
							mimetypeChecked = true;
						}
						processEntry(entry);
					}
				}
			} catch (IOException e) {
				throw new EPUB3ReaderException(e);
			}
		} finally {
			if (zis != null) {
				try {
					zis.close();
				} catch (IOException e) {
					if (logger.isLoggable(Level.FINE)) {
						logger.log(Level.FINE, "Failed to close zip input stream.", e);
					}
				}
			}
		}
	}
	
	private void processMimetype(ZipEntry entry) throws EPUB3ReaderException, IOException {
		int s = (int) entry.getSize();
		byte[] b = new byte[s];
		zis.read(b);
		if (!"application/epub+zip".equals(new String(b))) {
			throw new EPUB3ReaderException("Wrong mimetype: " + new String(b));
		}
	}

	protected void processEntry(ZipEntry entry) throws EPUB3ReaderException {
		File f = new File(output, entry.getName());
		try {
			if (entry.isDirectory()) {
				f.mkdirs();
			} else {
				f.getParentFile().mkdirs();
				try {
					writeToStream(new FileOutputStream(f));
				} catch (FileNotFoundException e) {
					throw new EPUB3ReaderException(e);
				}
			}
		} finally {
			try {
				zis.closeEntry();
			} catch (IOException e) {
				if (logger.isLoggable(Level.FINE)) {
					logger.log(Level.FINE, "Failed to close zip entry.", e);
				}
			}
		}
	}

	protected void writeToStream(OutputStream os) throws EPUB3ReaderException {
		int buf = 2048;
		byte[] b = new byte[buf];
		int read = 0;
		try {
			while ((read = zis.read(b)) > -1) {
				os.write(b, 0, read);
			}
		} catch (IOException e) {
			throw new EPUB3ReaderException(e);
		} finally {
			try {
				os.close();
			} catch (IOException e) {
				if (logger.isLoggable(Level.FINE)) {
					logger.log(Level.FINE, "Failed to close output stream.", e);
				}
			}
		}
	}

}
