package org.daisy.common.messaging;

abstract class AbstractLogger extends org.daisy.common.slf4j.AbstractLogger {

	protected abstract void logMessage(MessageBuilder message);

	private void logMessage(String msg, Message.Level level) {
		logMessage(
			new MessageBuilder()
			    .withLevel(level)
			    .withText(msg));
	}

	public boolean isTraceEnabled() {
		return true;
	}

	public boolean isDebugEnabled() {
		return true;
	}

	public boolean isInfoEnabled() {
		return true;
	}

	public boolean isWarnEnabled() {
		return true;
	}

	public boolean isErrorEnabled() {
		return true;
	}

	protected void doTrace(String msg) {
		logMessage(msg, Message.Level.TRACE);
	}

	protected void doDebug(String msg) {
		logMessage(msg, Message.Level.DEBUG);
	}

	protected void doInfo(String msg) {
		logMessage(msg, Message.Level.INFO);
	}

	protected void doWarn(String msg) {
		logMessage(msg, Message.Level.WARNING);
	}

	protected void doError(String msg) {
		logMessage(msg, Message.Level.ERROR);
	}
}
