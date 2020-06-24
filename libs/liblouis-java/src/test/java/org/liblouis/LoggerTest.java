package org.liblouis;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LoggerTest {
	
	@Test
	public void testLogger() {
		logger.reset();
		try { new Translator("unexisting_file"); }
		catch (CompilationException e) {}
		assertEquals("[ERROR] Cannot resolve table 'unexisting_file'" + System.getProperty("line.separator") +
		             "[ERROR] 1 errors found."                        + System.getProperty("line.separator") +
		             "[ERROR] unexisting_file could not be compiled"  + System.getProperty("line.separator"),
		             logger.toString());
		logger.reset();
		Louis.setLogLevel(Logger.Level.FATAL);
		try { new Translator("unexisting_file"); }
		catch (CompilationException e) {}
		assertEquals("", logger.toString());
	}
	
	private final ByteArrayLogger logger;
	
	public LoggerTest() {
		logger = new ByteArrayLogger() {
			public String format(Logger.Level level, String message) {
				switch (level) {
				case DEBUG: return "[DEBUG] " + message;
				case INFO: return "[INFO] " + message;
				case WARN: return "[WARN] " + message;
				case ERROR: return "[ERROR] " + message;
				case FATAL: return "[FATAL] " + message; }
				return null;
			}
		};
		Louis.setLogger(logger);
	}
	
	private abstract class ByteArrayLogger implements Logger {
		private ByteArrayOutputStream stream = new ByteArrayOutputStream();
		private PrintStream printStream = new PrintStream(stream);
		public abstract String format(Logger.Level level, String message);
		public void log(Logger.Level level, String message) {
			String formattedMessage = format(level, message);
			if (formattedMessage != null)
				printStream.println(formattedMessage);
		}
		public void reset() {
			stream.reset();
		}
		public String toString() {
			return stream.toString();
		}
	}
}
