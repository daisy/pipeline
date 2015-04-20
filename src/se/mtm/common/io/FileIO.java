package se.mtm.common.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;

public class FileIO {

	/**
	 * Copies an input stream to an output stream
	 * 
	 * @param is
	 *            the input stream
	 * @param os
	 *            the output stream
	 * @throws IOException
	 *             if IO fails
	 */
	public static void copy(InputStream is, OutputStream os) throws IOException {
		InputStream bis = new BufferedInputStream(is);
		OutputStream bos = new BufferedOutputStream(os);
		int b;
		while ((b = bis.read()) != -1) {
			bos.write(b);
		}
		bos.flush();
		bos.close();
		bis.close();
	}

	public static void copy(File input, File output) throws IOException {
		copy(new FileInputStream(input), new FileOutputStream(output));
	}

	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	public static File createTempFile() throws IOException {
		File ret = File.createTempFile("temp", null, null);
		ret.deleteOnExit();
		return ret;
	}

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

	public static void copyRecursive(File f, File out) {
		if (f.isDirectory()) {
			out.mkdirs();
			for (File f1 : f.listFiles()) {
				copyRecursive(f1, new File(out, f1.getName()));
			}
		} else {
			try {
				copyFile(f, out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

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
	 * @param f1
	 *            the first input stream
	 * @param f2
	 *            the second input stream
	 * @return returns -1 if the streams are equal, byte position where first
	 *         difference occurred otherwise
	 * @throws IOException
	 *             if IO fails
	 */
	public static long diff(InputStream f1, InputStream f2) throws IOException {
		InputStream bf1 = new BufferedInputStream(f1);
		InputStream bf2 = new BufferedInputStream(f2);
		try {
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
		} finally {
			bf1.close();
			bf2.close();
		}
	}
}
