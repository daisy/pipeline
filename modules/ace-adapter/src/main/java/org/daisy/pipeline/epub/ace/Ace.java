package org.daisy.pipeline.epub.ace;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.io.CharStreams;

import org.daisy.common.shell.CommandRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ace {

	final static Logger LOGGER = LoggerFactory.getLogger(Ace.class);

	private final File executableFile;
	private final String version;

	/**
	 * @throws RuntimeException if the executable could not be successfully verified
	 */
	Ace(File executableFile) throws RuntimeException {
		this.executableFile = executableFile;
		StringBuilder v = new StringBuilder();
		try {
			newCommand().withArgument("-v").runner().consumeOutput(
				stream -> {
					try (Reader r = new InputStreamReader(stream)) {
						v.append(CharStreams.toString(r)); }})
				.run();
			this.version = v.toString().trim();
		} catch (Throwable e) {
			throw new RuntimeException("Could not retrieve version of Ace", e);
		}
	}

	/**
	 * Get the version of Ace
	 */
	public String getVersion() {
		return version;
	}

	public CommandBuilder newCommand() {
		return new CommandBuilder();
	}

	public class CommandBuilder {
		private CommandBuilder() {}

		private final List<String> extraArgs = new ArrayList<>();

		public CommandBuilder withArgument(String arg) {
			extraArgs.add(arg);
			return this;
		}

		public CommandRunner runner() {
			List<String> cmd = new ArrayList<>();
			cmd.add(executableFile.getAbsolutePath());
			for (String arg : extraArgs)
				cmd.add(arg);
			CommandRunner runner = new CommandRunner(cmd);
			runner = runner.consumeError(LOGGER);
			return runner;
		}
	}
}
