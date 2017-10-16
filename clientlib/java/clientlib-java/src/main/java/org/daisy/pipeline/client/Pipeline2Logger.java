package org.daisy.pipeline.client;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class Pipeline2Logger {
	/**
	 * Logger levels.
	 * {@code
	 * ALL > TRACE > DEBUG > INFO > WARN > ERROR > FATAL > OFF.
	 * ALL -The ALL Level has the lowest possible rank and is intended to turn on all logging. In practice the same as the TRACE level.
	 * TRACE - The TRACE Level designates finer-grained informational events than the DEBUG level.
	 * DEBUG - The DEBUG Level designates fine-grained informational events that are most useful to debug an application.
	 * INFO – The INFO level designates informational messages that highlight the progress of the application at coarse-grained level.
	 * WARN – The WARN level designates potentially harmful situations.
	 * ERROR – The ERROR level designates error events that might still allow the application to continue running.
	 * FATAL – The FATAL level designates very severe error events that will presumably lead the application to abort.
	 * OFF – The OFF Level has the highest possible rank and is intended to turn off logging.
	 * }
	 */
	public static enum LEVEL {
		ALL, TRACE, DEBUG, INFO, WARN, ERROR, FATAL, OFF
	}


	private static Pipeline2Logger logger;
	static {
		logger = new Pipeline2ConsoleLogger();
	}
	
	/**
	 * Get the currently used Pipeline2Logger instance in use.
	 * 
	 * The default logger used if nothing else is set is logging to the console. 
	 * 
	 * @return the Pipeline2Logger in use
	 */
	public static Pipeline2Logger logger() {
		return logger;
	}
	
	/**
	 * Set the Pipeline2Logger instance to be used for logging.
	 * 
	 * @param logger the Pipeline2Logger to use
	 */
	public static void setLogger(Pipeline2Logger logger) {
		Pipeline2Logger.logger = logger;
	}

	/** Default logging implementation that logs to the console. */
	public static class Pipeline2ConsoleLogger extends Pipeline2Logger {
		private LEVEL level = LEVEL.INFO;
		
		@Override
		public void setLevel(LEVEL level) {
			if (level != null)
				this.level = level;
		}
		
		@Override
		public LEVEL getLevel() {
			return level;
		}

		@Override
		public boolean logsLevel(LEVEL level) {
			return this.level.ordinal() <= level.ordinal();
		}

		@Override
		public void trace(String message) {
			if (!logsLevel(LEVEL.TRACE)) return;
			System.err.println("[trace] "+message);
		}

		@Override
		public void trace(String message, Exception e) {
			if (!logsLevel(LEVEL.TRACE)) return;
			System.err.println("[trace] "+message);
			System.err.println("[trace] "+stacktraceToString(e));
		}

		@Override
		public void debug(String message) {
			if (!logsLevel(LEVEL.DEBUG)) return;
			System.out.println("[debug] "+message);
		}

		@Override
		public void debug(String message, Exception e) {
			if (!logsLevel(LEVEL.DEBUG)) return;
			System.out.println("[debug] "+message);
			System.err.println("[debug] "+stacktraceToString(e));
		}

		@Override
		public void info(String message) {
			if (!logsLevel(LEVEL.INFO)) return;
			System.out.println("[info] "+message);
		}

		@Override
		public void info(String message, Exception e) {
			if (!logsLevel(LEVEL.INFO)) return;
			System.out.println("[info] "+message);
			System.err.println("[info] "+stacktraceToString(e));
		}

		@Override
		public void warn(String message) {
			if (!logsLevel(LEVEL.WARN)) return;
			System.out.println("[warn] "+message);
		}

		@Override
		public void warn(String message, Exception e) {
			if (!logsLevel(LEVEL.WARN)) return;
			System.out.println("[warn] "+message);
			System.err.println("[warn] "+stacktraceToString(e));
		}

		@Override
		public void error(String message) {
			if (!logsLevel(LEVEL.ERROR)) return;
			System.err.println("[error] "+message);
		}

		@Override
		public void error(String message, Exception e) {
			if (!logsLevel(LEVEL.ERROR)) return;
			System.err.println("[error] "+message);
			System.err.println("[error] "+stacktraceToString(e));
		}

		@Override
		public void fatal(String message) {
			if (!logsLevel(LEVEL.FATAL)) return;
			System.err.println("[fatal] "+message);
		}

		@Override
		public void fatal(String message, Exception e) {
			if (!logsLevel(LEVEL.FATAL)) return;
			System.err.println("[fatal] "+message);
			System.err.println("[fatal] "+stacktraceToString(e));
		}

		public String stacktraceToString(Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return sw.toString();
		}

	}
	
	// ---------- below this is the methods that loggers must implement ----------
	
	/** Set the logger level.
	 * 
	 *  @param level the logger level to use
	 */
	public abstract void setLevel(LEVEL level);
	
	/** Gets the logger level.
	 * 
	 *  @return the logger Level
	 */
	public abstract LEVEL getLevel();

	/** Returns whether or not messages of the given level will be logged.
	 * 
	 *  @param level the logger level
	 *  @return whether or not the logger will log messages at this level
	 */
	public abstract boolean logsLevel(LEVEL level);

	/** The TRACE Level designates finer-grained informational events than the DEBUG level.
	 *  
	 *  @param message the message to log
	 */
	public abstract void trace(String message);
	public abstract void trace(String message, Exception e);

	/** The DEBUG Level designates fine-grained informational events that are most useful to debug an application.
	 *  
	 *  @param message the message to log
	 */
	public abstract void debug(String message);
	public abstract void debug(String message, Exception e);

	/** The INFO level designates informational messages that highlight the progress of the application at coarse-grained level.
	 *  
	 *  @param message the message to log
	 */
	public abstract void info(String message);
	public abstract void info(String message, Exception e);

	/** The WARN level designates potentially harmful situations.
	 *  
	 *  @param message the message to log
	 */
	public abstract void warn(String message);
	public abstract void warn(String message, Exception e);

	/** The ERROR level designates error events that might still allow the application to continue running.
	 *  
	 *  @param message the message to log
	 */
	public abstract void error(String message);
	public abstract void error(String message, Exception e);

	/** The FATAL level designates very severe error events that will presumably lead the application to abort.
	 *  
	 *  @param message the message to log
	 */
	public abstract void fatal(String message);
	public abstract void fatal(String message, Exception e);
}
