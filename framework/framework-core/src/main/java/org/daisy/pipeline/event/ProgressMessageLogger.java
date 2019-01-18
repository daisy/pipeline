package org.daisy.pipeline.event;

import org.daisy.common.messaging.Message;
import org.daisy.common.slf4j.AbstractLogger;

class ProgressMessageLogger extends AbstractLogger {

	private final ProgressMessage message;

	ProgressMessageLogger(ProgressMessage message) {
		this.message = message;
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
		postMessage(msg, Message.Level.TRACE);
	}

	protected void doDebug(String msg) {
		postMessage(msg, Message.Level.DEBUG);
	}

	protected void doInfo(String msg) {
		postMessage(msg, Message.Level.INFO);
	}

	protected void doWarn(String msg) {
		postMessage(msg, Message.Level.WARNING);
	}

	protected void doError(String msg) {
		postMessage(msg, Message.Level.ERROR);
	}

	private void postMessage(String msg, Message.Level level) {
		message.post(
			new ProgressMessageBuilder()
			    .withLevel(level)
			    .withText(msg)
		).close();
	}
}
