package org.daisy.pipeline.job.impl;

import java.util.function.Consumer;

import org.daisy.common.messaging.AbstractMessageAccessor;
import org.daisy.common.messaging.Message;
import org.daisy.common.properties.Properties;
import org.daisy.pipeline.event.MessageStorage;
import org.daisy.pipeline.job.JobId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobMessageAccessorFromStorage extends AbstractMessageAccessor {

	private static Logger logger = LoggerFactory.getLogger(JobMessageAccessorFromStorage.class);

	private final JobId id;
	private final MessageStorage storage;

	public JobMessageAccessorFromStorage(JobId id, MessageStorage storage) {
		this(id, storage, Message.Level.TRACE);
	}

	public JobMessageAccessorFromStorage(JobId id, MessageStorage storage, Message.Level threshold) {
		super(threshold);
		this.id = id;
		this.storage = storage;
		logger.trace("Created MessageAccessorFromStorage for job " + id + " and message storage " + storage);
	}

	@Override
	protected Iterable<Message> allMessages() {
		return storage.get(id.toString());
	}

	// It is assumed that messages are immutable once stored and that no new messages are produced.
	@Override
	public void listen(Consumer<Integer> callback) {}
	@Override
	public void unlisten(Consumer<Integer> callback) {}

}
