package org.daisy.pipeline.pandoc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import org.daisy.common.file.URLs;
import org.daisy.common.properties.Properties;
import org.daisy.common.shell.BinaryFinder;
import org.daisy.common.shell.CommandRunner;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "Pandoc",
	service = { Pandoc.class }
)
public class Pandoc {

	private final static Logger LOGGER = LoggerFactory.getLogger(Pandoc.class);

	private final Supplier<File> executableFile;
	private File tmpDirectory = null;
	private final Map<String,File> luaFilters = new HashMap<>();

	public Pandoc() {
		Optional<Supplier<File>> exec = findPandoc();
		if (!exec.isPresent())
			throw new RuntimeException("Pandoc executable not found");
		executableFile = Suppliers.memoize(exec.get());
	}

	public CommandBuilder newCommand() {
		return new CommandBuilder();
	}

	public enum Format {

		MARKDOWN("markdown"),
		HTML("html"),
		DOCX("docx");

		private final String format;

		private Format(String format) {
			this.format = format;
		}
	}

	public enum Filter {

		DETECT_IMAGE_CAPTIONS("detect-image-captions.lua");

		private final String fileName;

		private Filter(String fileName) {
			this.fileName = fileName;
		}
	}

	public class CommandBuilder {
		private CommandBuilder() {}

		private String inputFormat = null;
		private String outputFormat = null;
		private File inputFile = null;
		private File outputFile = null;
		private final List<File> filters = new ArrayList<>();
		private final List<String> extraArgs = new ArrayList<>();

		public CommandBuilder withInputFormat(Format format) {
			return withInputFormat(format.format);
		}

		public CommandBuilder withInputFormat(String format) {
			inputFormat = format;
			return this;
		}

		public CommandBuilder withOutputFormat(Format format) {
			return withOutputFormat(format.format);
		}

		public CommandBuilder withOutputFormat(String format) {
			outputFormat = format;
			return this;
		}

		public CommandBuilder withInput(File input) throws FileNotFoundException {
			if (!input.isFile())
				throw new FileNotFoundException("File does not exist: " + input);
			inputFile = input;
			return this;
		}

		public CommandBuilder withOutput(File output) {
			outputFile = output;
			return this;
		}

		public CommandBuilder withFilter(Filter filter) throws FileNotFoundException, IOException {
			return withFilter(getLuaFilter(filter.fileName));
		}

		public CommandBuilder withFilter(File filter) throws FileNotFoundException {
			if (!filter.isFile())
				throw new FileNotFoundException("File does not exist: " + filter);
			filters.add(filter);
			return this;
		}

		public CommandBuilder withArgument(String arg) {
			extraArgs.add(arg);
			return this;
		}

		public CommandRunner runner() {
			List<String> cmd = new ArrayList<>();
			cmd.add(getExecutable().getAbsolutePath());
			if (inputFormat != null) {
				cmd.add("-f");
				cmd.add(inputFormat);
			}
			if (outputFormat != null) {
				cmd.add("-t");
				cmd.add(outputFormat);
			}
			if (outputFile != null) {
				cmd.add("-o");
				cmd.add(outputFile.getAbsolutePath());
			}
			for (File f : filters)
				cmd.add("--lua-filter=" + f.getAbsolutePath());
			for (String arg : extraArgs)
				cmd.add(arg);
			if (inputFile != null)
				cmd.add(inputFile.getAbsolutePath());
			CommandRunner runner = new CommandRunner(cmd);
			runner = runner.consumeError(LOGGER);
			return runner;
		}
	}

	private File getExecutable() {
		return executableFile.get();
	}

	private File getLuaFilter(String fileName) throws FileNotFoundException, IOException {
		File filter = luaFilters.get(fileName);
		if (filter == null) {
			URL url = URLs.getResourceFromJAR("lua/" + fileName, Pandoc.class);
			if (url == null)
				throw new FileNotFoundException("Could not find resource in JAR: lua/" + fileName);
			try {
				filter = new File(URLs.asURI(url));
			} catch (IllegalArgumentException iae) {
				filter = new File(getTempDirectory(), fileName);
				filter.deleteOnExit();
				copy(url, filter);
			}
			luaFilters.put(fileName, filter);
		}
		return filter;
	}

	private File getTempDirectory() throws IOException {
		if (tmpDirectory == null) {
			tmpDirectory = Files.createTempDirectory("pipeline-").toFile();
			tmpDirectory.deleteOnExit();
		}
		return tmpDirectory;
	}

	/* Utilities **/

	private Optional<Supplier<File>> findPandoc() {
		String path = Properties.getProperty("org.daisy.pipeline.pandoc.path");
		if (path != null && !"".equals(path)) {
			File file = new File(path);
			if (file.isFile())
				return Optional.of(() -> file);
			else
				LOGGER.warn("File does not exist: " + path);
		}
		Optional<Supplier<File>> file = findPandocInJAR();
		if (file.isPresent())
			return file;
		return BinaryFinder.find("pandoc").map(f -> () -> new File(f));
	}

	private Optional<Supplier<File>> findPandocInJAR() {
		String tarPath = "native.tar.bz2";
		URL tarURL = URLs.getResourceFromJAR(tarPath, Pandoc.class);
		if (tarURL == null)
			throw new IllegalStateException("Could not find file in JAR: " + tarPath); // should not happen
		String pandocPath;
		String pandocFileName; {
			String os = System.getProperty("os.name");
			String path;
			if (os.toLowerCase().startsWith("windows")) {
				path = "native/windows/";
				pandocFileName = "pandoc.exe";
			} else if (os.toLowerCase().startsWith("mac os x")) {
				path = "native/macosx/";
				pandocFileName = "pandoc";
			} else {
				LOGGER.warn("Pandoc is not available on this platform (" + os + ")"
				            + " unless installed manually.");
				return Optional.empty();
			}
			String arch = System.getProperty("os.arch").toLowerCase();
			if ("amd64".equals(arch) ||
			    "x86_64".equals(arch))
				path += "x86_64/";
			else if ("arm64".equals(arch) ||
			         "aarch64".equals(arch))
				path += "aarch64/";
			else {
				LOGGER.warn("Pandoc is not available on this platform (" + os + "-" + arch + ")"
				            + " unless installed manually.");
				return Optional.empty();
			}
			pandocPath = path + pandocFileName;

			// check that the file is present in the tar
			// FIXME: skipped for now because it takes considerable time
			/*boolean presentInTar = false; {
				try (BufferedInputStream bis = new BufferedInputStream(tarURL.openStream());
				     CompressorInputStream cis = new CompressorStreamFactory()
				         .createCompressorInputStream(CompressorStreamFactory.BZIP2, bis);
				     TarArchiveInputStream tar = new TarArchiveInputStream(cis)) {
					//tar.read();
					while (true) {
						TarArchiveEntry e = tar.getNextTarEntry();
						if (e == null)
							break;
						else if (pandocPath.equals(e.getName())) {
							presentInTar = true;
							break;
						}
					}
				} catch (IOException|CompressorException e) {
					LOGGER.debug("Could not find pandoc in JAR", e); // should not happen
				}
			}
			if (!presentInTar) {
				LOGGER.debug("Could not find pandoc in JAR: " + pandocPath);
				return Optional.empty();
			}*/
		}
		return Optional.of(
			() -> {
				try (BufferedInputStream bis = new BufferedInputStream(tarURL.openStream());
				     CompressorInputStream cis = new CompressorStreamFactory()
				         .createCompressorInputStream( CompressorStreamFactory.BZIP2, bis);
				     TarArchiveInputStream tar = new TarArchiveInputStream(cis)) {
					//tar.read();
					while (true) {
						TarArchiveEntry entry = tar.getNextTarEntry();
						if (entry != null && pandocPath.equals(entry.getName())) {
							File file = new File(getTempDirectory(), pandocFileName);
							file.deleteOnExit();
							copy(tar, file);
							if (!System.getProperty("os.name").toLowerCase().startsWith("windows"))
								try {
									Runtime.getRuntime().exec(
										new String[] { "chmod", "775", file.getAbsolutePath() }).waitFor();
								} catch (IOException|InterruptedException e) {
									throw new IllegalStateException(
										"Unexpected error happened while preparing Pandoc executable", e);
								}
							return file;
						}
					}
				} catch (Throwable e) {
					throw new IllegalStateException(
						"Unexpected error happened while unpacking Pandoc executable", e);
				}
			}
		);
	}

	private static void copy(URL url, File file) throws IOException {
		url.openConnection();
		try (InputStream reader = url.openStream()) {
			copy(reader, file);
		}
	}

	private static void copy(InputStream stream, File file) throws IOException {
		file.getParentFile().mkdirs();
		file.createNewFile();
		try (FileOutputStream writer = new FileOutputStream(file)) {
			byte[] buffer = new byte[153600];
			int bytesRead = 0;
			while ((bytesRead = stream.read(buffer)) > 0) {
				writer.write(buffer, 0, bytesRead);
				buffer = new byte[153600];
			}
		}
	}
}
