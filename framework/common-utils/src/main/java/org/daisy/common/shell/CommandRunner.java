package org.daisy.common.shell;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import com.google.common.io.CharStreams;

import org.slf4j.Logger;

public class CommandRunner {

	private static final File devNull = System.getProperty("os.name").toLowerCase().startsWith("windows")
		? new File("NUL")
		: new File("/dev/null");

	private final String[] command;
	private Consumer<InputStream> outputConsumer;
	private Consumer<InputStream> errorConsumer;
	private Consumer<OutputStream> inputFeeder;

	public CommandRunner(String... command) {
		this.command = command;
	}

	/**
	 * @param consumer The normal output of the subprocess is passes as an InputStream to this object.
	 *                 If null, the stream is discarded.
	 */
	public CommandRunner consumeOutput(Consumer<InputStream> consumer) {
		outputConsumer = consumer;
		return this;
	}

	/**
	 * @param consumer The error output of the subprocess is passed as an InputStream to this object.
	 *                 If null, the stream is discarded.
	 */
	public CommandRunner consumeError(Consumer<InputStream> consumer) {
		errorConsumer = consumer;
		return this;
	}

	/**
	 * @param logger If the subprocess generated error output, write it to this Logger object.
	 */
	public CommandRunner consumeError(Logger logger) {
		errorConsumer = stream -> {
			try (Reader r = new InputStreamReader(stream)) {
				String error = CharStreams.toString(r);
				if (error != null && !error.isEmpty())
					logger.error(error);
			}
		};
		return this;
	}

	/**
	 * @param feeder The normal input of the subprocess is passed as an OutputStream to this object.
	 */
	public CommandRunner feedInput(Consumer<OutputStream> feeder) {
		this.inputFeeder = feeder;
		return this;
	}

	/**
	 * @param bytes These bytes are written to the normal input of the subprocess.
	 */
	public CommandRunner feedInput(byte[] bytes) {
		this.inputFeeder = stream -> {
			stream.write(bytes);
			stream.close();
		};
		return this;
	}

	/**
	 * Run the command and wait for the process to finish.
	 *
	 * @return The exit value.
	 */
	public int run() throws Throwable {
		return run((Supplier<Long>)null);
	}

	public int run(Supplier<Long> timeout) throws TimeoutException, Throwable {
		ProcessBuilder pb = new ProcessBuilder();
		pb.command(command);
		if (outputConsumer == null)
			pb.redirectOutput(devNull);
		if (errorConsumer == null)
			pb.redirectError(devNull);
		final Process p = pb.start();
		try {
			if (inputFeeder != null)
				inputFeeder.accept(p.getOutputStream());
			if (errorConsumer != null && outputConsumer != null) {
				ExecutorService executor = Executors.newFixedThreadPool(2);
				CompletionService<Void> completionService =
					new ExecutorCompletionService<Void>(executor);
				completionService.submit(() -> { outputConsumer.accept(p.getInputStream()); return null; });
				completionService.submit(() -> { errorConsumer.accept(p.getErrorStream()); return null; });
				for (int done = 0; done < 2; done++)
					try {
						completionService.take().get();
					} finally {
						executor.shutdownNow();
					}
			} else if (errorConsumer != null)
				errorConsumer.accept(p.getErrorStream());
			else if (outputConsumer != null)
				outputConsumer.accept(p.getInputStream());
			if (timeout == null)
				return p.waitFor();
			else {
				long wait = 0;
				do {
					wait = timeout.get();
					boolean terminated = p.waitFor(wait, TimeUnit.MILLISECONDS);
					if (terminated)
						return p.exitValue();
				} while (wait > 0);
				throw new TimeoutException();
			}
		} catch (Throwable e) {
			if (e instanceof InterruptedException)
				throw new InterruptedException("Interrupted while running command `" + String.join(" ", command) + "`");
			while (e instanceof ExecutionException) e = ((ExecutionException)e).getCause();
			throw e;
		} finally {
			p.destroy();
		}
	}

	public static int run(String... command) throws Throwable {
		return new CommandRunner(command).run();
	}

	/**
	 * Version of java.util.function.Consumer that can throw checked exceptions.
	 */
	@FunctionalInterface
	public static interface Consumer<T> {
		void accept(T t) throws Exception;
	}
}
