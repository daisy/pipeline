package org.daisy.pipeline.event;

import com.google.common.base.Supplier;
import com.google.common.eventbus.EventBus;

import org.daisy.common.messaging.Message;
import org.daisy.common.slf4j.AbstractLogger;

import org.slf4j.Logger;
import org.slf4j.MDC;

public class EventBusProvider implements Supplier<EventBus>{

	private final EventBus mEventBus=new EventBus();//AsyncEventBus(Executors.newFixedThreadPool(10));

	@Override
	public EventBus get() {
		return mEventBus;
	}

	/**
	 * Post a ProgressMessage to the bus, and post a ProgressMessageUpdate event every
	 * time the object is updated.
	 */
	public ProgressMessage post(ProgressMessageBuilder message) {
		message.onUpdated(e -> EventBusProvider.this.get().post(e));
		ProgressMessage m = message.build();
		get().post(m);
		return m;
	}

	/**
	 * SLF4J Logger that sends Message events with the job id of the current job to the
	 * EventBus.
	 */
	public Logger getAsLogger() {
		return asLogger;
	}

	private final Logger asLogger = new AbstractLogger() {
		
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

		// depends on MDC manipulation of DefaultJobExecutionService
		private void postMessage(String msg, Message.Level level) {
			String jobId = MDC.get("jobid");
			if (jobId != null) {
				ProgressMessageBuilder m = new ProgressMessageBuilder()
				                               .withJobId(jobId)
				                               .withLevel(level)
				                               .withText(msg);
				ProgressMessage activeBlock = ProgressMessage.getActiveBlock();
				if (activeBlock != null)
					activeBlock.post(m).close();
				// is null if this is a top-level message
				else
					EventBusProvider.this.post(m).close(); }
			else
				mEventBus.post(new Message.MessageBuilder()
				                          .withLevel(level)
				                          .withText(msg)
				                          .build());
		}
	};
}
