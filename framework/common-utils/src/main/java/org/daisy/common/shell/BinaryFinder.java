package org.daisy.common.shell;

import java.io.File;

import com.google.common.base.Optional;

public class BinaryFinder {

	/**
	 * Look for a given executable in the PATH environment variable.
	 * 
	 * @return the absolute path of the executable if it exists, an absent
	 *         optional otherwise.
	 */
	public static Optional<String> find(String executableName) {
		String os = System.getProperty("os.name");
		String[] extensions;
		if (os != null && os.startsWith("Windows"))
			extensions = winExtensions;
		else
			extensions = nixExtensions;

		return find(executableName, extensions, System.getenv("PATH"), File.pathSeparator);
	}

	static Optional<String> find(String executableName, String[] extensions,
	        String systemPath, String pathSeparator) {
		if (systemPath == null || pathSeparator == null)
			return Optional.absent();

		String[] pathDirs = systemPath.split(pathSeparator);
		for (String ext : extensions) {
			String fullname = executableName + ext;
			for (String pathDir : pathDirs) {
				File file = new File(pathDir, fullname);
				if (file.isFile()) {
					return Optional.of(file.getAbsolutePath());
				}
			}
		}
		return Optional.absent();
	}

	private static final String[] winExtensions = {
	        ".exe", ".bat", ".cmd", ".bin", ""
	};
	private static final String[] nixExtensions = {
	        "", ".run", ".bin", ".sh"
	};

}
