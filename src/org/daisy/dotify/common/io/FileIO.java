package org.daisy.dotify.common.io;

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

/**
 * Provides file IO tools.
 * @author Joel Håkansson
 *
 */
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
	 * Copies an input file to an output file
	 * @param input the input file
	 * @param output the output file
	 * @throws IOException if IO fails
	 */
	public static void copy(File input, File output) throws IOException {
		copy(new FileInputStream(input), new FileOutputStream(output));
	}

	/**
	 * Copies an input file to an output file
	 * @param sourceFile the source file
	 * @param destFile the destination file
	 * @throws IOException if IO fails
	 */
	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}
		try (
			FileInputStream is = new FileInputStream(sourceFile);
			FileChannel source = is.getChannel();
			FileOutputStream os = new FileOutputStream(destFile);
			FileChannel destination = os.getChannel();
		) {
			destination.transferFrom(source, 0, source.size());
		}
	}

	/**
	 * Creates a temp file
	 * @return returns a the created file
	 * @throws IOException if IO fails
	 */
	public static File createTempFile() throws IOException {
		File ret = File.createTempFile("temp", null, null);
		ret.deleteOnExit();
		return ret;
	}

	/**
	 * Creates a temp folder
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
	 * Copies a folder recursively
	 * @param f the source folder
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
				copyFile(f, out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Deletes a folder recursively
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
}
