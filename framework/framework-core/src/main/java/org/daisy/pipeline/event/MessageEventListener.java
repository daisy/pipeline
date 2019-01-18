package org.daisy.pipeline.event;

import java.util.List;
import java.util.LinkedList;

import org.daisy.common.messaging.Message;

import com.google.common.eventbus.Subscribe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class receives message events and forwards them to sub-listeners per job
 * (LiveMessageAccessor instances).
 */
public class MessageEventListener {

	private static Logger logger = LoggerFactory.getLogger(MessageEventListener.class);

	private EventBusProvider eventBusProvider;

	public void setEventBusProvider(EventBusProvider eventBusProvider) {
		this.eventBusProvider = eventBusProvider;
		this.eventBusProvider.get().register(this);
	}

	private final List<LiveMessageAccessor> listeners = new LinkedList<>();

	synchronized void listen(LiveMessageAccessor listener) {
		listeners.add(listener);
	}

	synchronized void unlisten(LiveMessageAccessor listener) {
		listeners.remove(listener);
	}

	@Subscribe
	public synchronized void handleMessage(ProgressMessage msg) {
		String jobId = msg.getJobId();
		logger.trace("received message event: [job: " + jobId + ", msg: " + msg.getSequence() + "]");
		for (LiveMessageAccessor l : listeners)
			if (l.id.equals(jobId))
				l.handleMessage(msg);
	}

	@Subscribe
	public void handleMessageUpdate(ProgressMessageUpdate update) {
		Message msg = update.getMessage();
		String jobId = msg.getJobId();
		logger.trace("received message update event: [job: " + jobId + ", msg: " + msg.getSequence()
		             + ", event: " + update.getSequence() + "]");
		for (LiveMessageAccessor l : listeners)
			if (l.id.equals(jobId))
				l.handleMessageUpdate(update);
	}
}
