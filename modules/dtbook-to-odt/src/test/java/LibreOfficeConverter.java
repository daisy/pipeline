import org.daisy.common.shell.CommandRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.io.CharStreams;

import org.daisy.common.shell.CommandRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibreOfficeConverter {

	public enum Format {

		DOCX("docx");

		private final String format;

		private Format(String format) {
			this.format = format;
		}

		@Override
		public String toString() {
			return format;
		}
	}

	final static Logger LOGGER = LoggerFactory.getLogger(LibreOfficeConverter.class);

	private final File executableFile;
	private final String version;

	/**
	 * @throws RuntimeException if the executable could not be successfully verified
	 */
	LibreOfficeConverter(File executableFile) throws RuntimeException {
		this.executableFile = executableFile;
		StringBuilder v = new StringBuilder();
		try {
			newCommand().withArgument("--version").runner().consumeOutput(
				stream -> {
					try (Reader r = new InputStreamReader(stream)) {
						v.append(CharStreams.toString(r)); }})
				.run();
			this.version = v.toString().trim().replaceAll("(?i)^libreoffice +", "");
		} catch (Throwable e) {
			throw new RuntimeException("Could not retrieve version of LibreOffice", e);
		}
	}

	/**
	 * Get the version of LibreOffice
	 */
	public String getVersion() {
		return version;
	}

	public CommandBuilder newCommand() {
		return new CommandBuilder();
	}

	public class CommandBuilder {
		private CommandBuilder() {}

		private File inputFile = null;
		private File outputDir = null;
		private Format outputFormat = null;
		private final List<String> extraArgs = new ArrayList<>();

		public CommandBuilder withInput(File input) throws FileNotFoundException {
			if (!input.isFile())
				throw new FileNotFoundException("File does not exist: " + input);
			inputFile = input;
			return this;
		}

		public CommandBuilder withOutputDir(File outputDir) {
			this.outputDir = outputDir;
			return this;
		}

		public CommandBuilder withOutputFormat(Format format) {
			outputFormat = format;
			return this;
		}

		public CommandBuilder withArgument(String arg) {
			extraArgs.add(arg);
			return this;
		}

		public CommandRunner runner() {
			List<String> cmd = new ArrayList<>();
			cmd.add(executableFile.getAbsolutePath());
			cmd.add("--headless");
			if (outputFormat != null) {
				cmd.add("--convert-to");
				cmd.add(outputFormat.toString());
			}
			if (outputDir != null) {
				cmd.add("--outdir");
				cmd.add(outputDir.getAbsolutePath());
			}
			if (inputFile != null) {
				cmd.add(inputFile.getAbsolutePath());
			}
			for (String arg : extraArgs)
				cmd.add(arg);
			CommandRunner runner = new CommandRunner(cmd);
			runner = runner.consumeError(LOGGER);
			return runner;
		}
	}
}
