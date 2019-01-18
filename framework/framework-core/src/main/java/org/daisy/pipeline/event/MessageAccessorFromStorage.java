package org.daisy.pipeline.event;

import java.util.function.BiConsumer;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.MessageAccessor;

public class MessageAccessorFromStorage extends AbstractMessageAccessor {

	private final MessageStorage storage;

	public MessageAccessorFromStorage(String id, MessageStorage storage) {
		super(id);
		this.storage = storage;
	}

	@Override
	protected Iterable<Message> allMessages() {
		return storage.get(id);
	}

	// It is assumed that messages are immutable once stored and that no new messages are produced.
	@Override
	public void listen(BiConsumer<MessageAccessor,Integer> callback) {}
	@Override
	public void unlisten(BiConsumer<MessageAccessor,Integer> callback) {}

}
