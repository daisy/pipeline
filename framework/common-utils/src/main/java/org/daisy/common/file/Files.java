package org.daisy.common.file;

import java.io.File;
import java.io.IOException;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.Path;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;

public final class Files {
	private Files() {}

	/**
	 * Delete the specified directory, together will all the files that it contains.
	 */
	public static boolean deleteDir(File dir) {
		File[] files = dir.listFiles();
		if (files != null)
			for (File f : files) {
				if (f.isDirectory())
					deleteDir(f);
				f.delete();
			}
		return dir.delete();
	}

	/**
	 * Delete the specified file or directory when the virtual machine
	 * terminates. A directory is deleted together with all the files
	 * that it contains at the time of this call, not at the time the
	 * virtual machine terminates.
	 */
	public static File deleteOnExit(File file) throws IOException {
		return deleteOnExit(file.toPath()).toFile();
	}

	public static Path deleteOnExit(Path file) throws IOException {
		java.nio.file.Files.walkFileTree(
				file,
				new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						file.toFile().deleteOnExit();
						return FileVisitResult.CONTINUE;
					}
				});
		return file;
	}
}
