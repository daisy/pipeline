package org.daisy.pipeline.event;

import java.util.function.BiConsumer;
import java.util.LinkedList;
import java.util.List;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.MessageAccessor;

/**
 * This class receives message events for a particular job, buffers them, makes them accessible
 * through a MessageFilte.
 */
public class LiveMessageAccessor extends AbstractMessageAccessor {

	private final MessageEventListener eventListener;
	private final List<BiConsumer<MessageAccessor,Integer>> callbacks;
	private final List<Message> messages;

	public LiveMessageAccessor(String id, MessageEventListener eventListener) {
		super(id);
		this.eventListener = eventListener;
		this.callbacks = new LinkedList<>();
		this.messages = new LinkedList<>();
		eventListener.listen(this);
	}

	@Override
	protected Iterable<Message> allMessages() {
		return messages;
	}

	@Override
	public void listen(BiConsumer<MessageAccessor,Integer> callback) {
		synchronized (callbacks) {
			callbacks.add(callback);
		}
	}

	@Override
	public void unlisten(BiConsumer<MessageAccessor,Integer> callback) {
		synchronized (callbacks) {
			callbacks.remove(callback);
		}
	}

	void handleMessage(ProgressMessage msg) {
		synchronized (messages) {
			messages.add(msg);
		}
		synchronized (callbacks) {
			for (BiConsumer<MessageAccessor,Integer> c : callbacks)
				c.accept(this, msg.getSequence());
		}
	}

	void handleMessageUpdate(ProgressMessageUpdate update) {
		synchronized (callbacks) {
			for (BiConsumer<MessageAccessor,Integer> c : callbacks)
				c.accept(this, update.getSequence());
		}
	}

	/**
	 * @param storage Store the buffered messages
	 */
	void store(MessageStorage storage) {
		synchronized (messages) {
			for (Message m : messages)
				storage.add(m);
		}
	}

	/** Stop listening to message updates. */
	void close() {
		eventListener.unlisten(this);
		synchronized (messages) {
			messages.clear();
		}
	}
}
