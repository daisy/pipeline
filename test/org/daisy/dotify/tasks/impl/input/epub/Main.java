package org.daisy.dotify.tasks.impl.input.epub;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import org.daisy.dotify.common.io.FileIO;

@SuppressWarnings("javadoc")
public class Main {

	/**
	 * @param args
	 * @throws FileNotFoundException
	 * @throws EPUB3ReaderException
	 * @throws IOException
	 */
	public static void main(String[] args) throws EPUB3ReaderException, IOException {
		if (args.length < 2) {
			System.out.println("Expected two arguments: path_to_epub path_to_result_folder");
			System.exit(-1);
		}
		File epub = new File(args[0]);
		if (!epub.exists()) {
			System.out.println("File does not exist: " + args[0]);
			System.exit(-2);
		}
		Logger logger = Logger.getLogger(Main.class.getCanonicalName());
		File unpacked = FileIO.createTempDir();
		try {
			logger.info("Unpacking...");
			ContentExtractor.unpack(new FileInputStream(epub), unpacked);
			ContentMerger.copyMerged(unpacked, new File(args[1]));
		} finally {
			logger.info("Deleting temp folder: " + unpacked);
			FileIO.deleteRecursive(unpacked);
		}
	}
}