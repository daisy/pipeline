package org.daisy.common.messaging;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Collection of messages that belong together, for example to a Pipeline job.
 */
public class MessageBus extends AbstractMessageAccessor implements MessageAppender {

	private final List<Message> messages = new LinkedList<>();
	private final List<Consumer<Integer>> callbacks = new LinkedList<>();
	private final Iterable<Message> messagesAsUnmodifiableList = Collections.unmodifiableList(messages);
	private final String ownerId;
	private boolean closed = false;

	/**
	 * @param ownerId A string that uniquely identifies this collection of messages. Could for
	 *                instance be the ID of a Pipeline job.
	 */
	public MessageBus(String ownerId, Message.Level threshold) {
		super(threshold);
		this.ownerId = ownerId;
	}

	@Override
	public MessageAppender append(MessageBuilder message) {
		if (closed)
			throw new UnsupportedOperationException("closed");
		MessageImpl msg = message.withOwnerId(ownerId)
		                         .onUpdated(this::notify)
		                         .build(null);
		synchronized (messages) {
			messages.add(msg);
		}
		notify(msg.getSequence());
		return msg;
	}

	@Override
	public void close() {
		closed = false;
	}

	@Override
	protected Iterable<Message> allMessages() {
		return messagesAsUnmodifiableList;
	}

	@Override
	public void listen(Consumer<Integer> callback) {
		synchronized (callbacks) {
			callbacks.add(callback);
		}
	}

	@Override
	public void unlisten(Consumer<Integer> callback) {
		synchronized (callbacks) {
			callbacks.remove(callback);
		}
	}

	private void notify(int sequence) {
		synchronized (callbacks) {
			for (Consumer<Integer> c : callbacks)
				c.accept(sequence);
		}
	}
}
