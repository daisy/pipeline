package org.daisy.pipeline.braille.common;

import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.Logger;

public interface JobContext extends Logger {;
	
	public abstract static class AbstractJobContext extends MarkerIgnoringBase implements JobContext {
		
		private static final long serialVersionUID = 1L;
		
		protected String format(String format, Object arg) {
			return MessageFormatter.format(format, arg).getMessage();
		}
		
		protected String format(String format, Object arg1, Object arg2) {
			return MessageFormatter.format(format, arg1, arg2).getMessage();
		}
		
		protected String format(String format, Object... args) {
			return MessageFormatter.format(format, args).getMessage();
		}
		
		protected abstract void doTrace(String msg);
		
		protected abstract void doDebug(String msg);
		
		protected abstract void doInfo(String msg);
		
		protected abstract void doWarn(String msg);
		
		protected abstract void doError(String msg);
		
		public void trace(String msg) {
			if (isTraceEnabled())
				doTrace(msg);
		}
		
		public void trace(String format, Object arg) {
			if (isTraceEnabled())
				doTrace(format(format, arg));
		}
		
		public void trace(String format, Object arg1, Object arg2) {
			if (isTraceEnabled())
				doTrace(format(format, arg1, arg2));
		}
		
		public void trace(String format, Object... args) {
			if (isTraceEnabled())
				doTrace(format(format, args));
		}
		
		public void trace(String msg, Throwable t) {
			if (isTraceEnabled())
				doTrace(msg);
		}
		
		public void debug(String msg) {
			if (isDebugEnabled())
				doDebug(msg);
		}
		
		public void debug(String format, Object arg) {
			if (isDebugEnabled())
				doDebug(format(format, arg));
		}
		
		public void debug(String format, Object arg1, Object arg2) {
			if (isDebugEnabled())
				doDebug(format(format, arg1, arg2));
		}
		
		public void debug(String format, Object... args) {
			if (isDebugEnabled())
				doDebug(format(format, args));
		}
		
		public void debug(String msg, Throwable t) {
			if (isDebugEnabled())
				doDebug(msg);
		}
		
		public void info(String msg) {
			if (isInfoEnabled())
				doInfo(msg);
		}
		
		public void info(String format, Object arg) {
			if (isInfoEnabled())
				doInfo(format(format, arg));
		}
		
		public void info(String format, Object arg1, Object arg2) {
			if (isInfoEnabled())
				doInfo(format(format, arg1, arg2));
		}
		
		public void info(String format, Object... args) {
			if (isInfoEnabled())
				doInfo(format(format, args));
		}
		
		public void info(String msg, Throwable t) {
			if (isInfoEnabled())
				doInfo(msg);
		}
		
		public void warn(String msg) {
			if (isWarnEnabled())
				doWarn(msg);
		}
		
		public void warn(String format, Object arg) {
			if (isWarnEnabled())
				doWarn(format(format, arg));
		}
		
		public void warn(String format, Object arg1, Object arg2) {
			if (isWarnEnabled())
				doWarn(format(format, arg1, arg2));
		}
		
		public void warn(String format, Object... args) {
			if (isWarnEnabled())
				doWarn(format(format, args));
		}
		
		public void warn(String msg, Throwable t) {
			if (isWarnEnabled())
				doWarn(msg);
		}
		
		public void error(String msg) {
			if (isErrorEnabled())
				doError(msg);
		}
		
		public void error(String format, Object arg) {
			if (isErrorEnabled())
				doError(format(format, arg));
		}
		
		public void error(String format, Object arg1, Object arg2) {
			if (isErrorEnabled())
				doError(format(format, arg1, arg2));
		}
		
		public void error(String format,  Object... args) {
			if (isErrorEnabled())
				doError(format(format, args));
		}
		
		public void error(String msg, Throwable t) {
			if (isErrorEnabled())
				doError(msg);
		}
	}
}
