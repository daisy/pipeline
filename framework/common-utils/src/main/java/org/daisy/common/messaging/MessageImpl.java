package org.daisy.common.messaging;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.MDC;

class MessageImpl extends ProgressMessage implements MessageAppender {

	final String ownerId;
	private final MessageImpl parent;
	private final BigDecimal portion;
	private final List<Consumer<Integer>> callbacks;
	private final Thread thread;
	private final MessageThread messageThread;
	private final List<MessageImpl> children = new ArrayList<MessageImpl>();
	private final List<ProgressMessage> unmodifiableChildren = new ArrayList<ProgressMessage>();
	private final List<ProgressMessage> unmodifiableListOfUnmodifiableChildren = Collections.unmodifiableList(unmodifiableChildren);

	private boolean closed = false;
	private int closeSequence = 0;

	static final ThreadLocal<MessageImpl> activeBlock = new ThreadLocal<>();
	static final Map<MessageThread,MessageImpl> activeBlockInThread = new HashMap<>();

	public MessageImpl(Throwable throwable, String text, Level level, int line,
	                   int column, Date timeStamp, Integer sequence, String ownerId,
	                   String file, MessageImpl parent, BigDecimal portion,
	                   List<Consumer<Integer>> callbacks) {
		super(throwable, text, level, line, column, timeStamp, sequence, ownerId, file);
		this.ownerId = ownerId;
		this.parent = parent;
		this.portion = portion;
		this.callbacks = callbacks;
		thread = Thread.currentThread();
		if (parent != null && parent.closed)
			throw new IllegalArgumentException("Parent must be open");
		if (parent != null && !ownerId.equals(parent.ownerId))
			throw new IllegalArgumentException("Message must belong to same owner as parent");
		MessageImpl active = activeBlock.get();
		if (active != null && active != parent)
			throw new RuntimeException("Only one active ProgressMessage allowed in the same thread");
		activeBlock.set(this);
		if (parent == null) {
			MessageThread t = new MessageThread(ownerId, null);
			if (activeBlockInThread.containsKey(t))
				messageThread = new MessageThread(ownerId, sequence);
			else
				messageThread = t;
			MDC.put("message-thread", messageThread.toString());
		} else if (thread == parent.thread)
			messageThread = parent.messageThread;
		else {
			messageThread = new MessageThread(ownerId, sequence);
			MDC.put("message-thread", messageThread.toString());
		}
		activeBlockInThread.put(messageThread, this);
	}

	public synchronized void close() {
		if (closed)
			throw new UnsupportedOperationException("Already closed");
		if (this != activeBlock.get()) {
			for (MessageImpl m : children)
				if (!m.closed)
					throw new UnsupportedOperationException("All children blocks must be closed before the parent can be closed");
			if (thread != Thread.currentThread())
				throw new IllegalStateException("A ProgressMessage must be created and closed in the same thread");
			else
				throw new RuntimeException("???");
		}
		BigDecimal lastProgress = getProgress();
		closed = true;
		closeSequence = messageCounts.get(ownerId);
		if (parent != null && thread == parent.thread) {
			activeBlock.set(parent);
			activeBlockInThread.put(parent.messageThread, parent);
		} else {
			activeBlock.remove();
			MDC.remove("message-thread");
			activeBlockInThread.remove(messageThread);
		}
		// a notification needs to be sent only if closing the message changes its progress
		if (lastProgress.compareTo(BigDecimal.ONE) < 0)
			updated(closeSequence, false);
	}

	public synchronized MessageAppender append(MessageBuilder message) {
		if (closed)
			throw new UnsupportedOperationException("Closed");
		synchronized(MUTEX) {
			MessageImpl m = message.build(this);
			children.add(m);
			unmodifiableChildren.add(unmodifiable(m));
			// if the message has no text, it is irrelevant so we don't have to send any notifications yet
			// however we don't ignore it completely because it may receive child messages later
			// the child messages will trigger their parent's updated() method
			// for now we don't notify at all about text-less messages, not even postponed,
			// because in practice notifying about the relevant child messages will be enough
			if (m.getText() != null)
				updated(m.getSequence(), true);
			return m;
		}
	}

	boolean isOpen() {
		return !closed;
	}

	int getCloseSequence() {
		if (!closed)
			throw new UnsupportedOperationException("Not closed yet");
		else
			return closeSequence;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public BigDecimal getPortion() {
		return portion;
	}

	private BigDecimal progress = BigDecimal.ZERO;

	public BigDecimal getProgress() {
		if (closed || portion.compareTo(BigDecimal.ZERO) == 0 || progress.compareTo(BigDecimal.ONE) >= 0)
			return BigDecimal.ONE;
		progress = BigDecimal.ZERO;
		synchronized(MUTEX) {
			for (MessageImpl m : children) {
				progress = progress.add(m.closed ? m.portion : m.portion.multiply(m.getProgress()));
				if (progress.compareTo(BigDecimal.ONE) >= 0)
					return BigDecimal.ONE;
			}
		}
		return progress;
	}

	void updated(int sequence, boolean textMessageAdded) {
		// either a descendant text message was added, or the progress has changed
		// if a text messages was added, we consider it relevant for all ancestors
		// if only the progress has changed, it is relevant for the parent only if the portion with the parent is non-zero
		if (callbacks != null && !callbacks.isEmpty()) {
			for (Consumer<Integer> callback : callbacks)
				callback.accept(sequence); }
		if (parent != null)
			if (textMessageAdded || portion.compareTo(BigDecimal.ZERO) > 0)
				parent.updated(sequence, textMessageAdded);
	}

	/**
	 * Returns read-only (but not immutable) elements. The iterator does not implement the remove()
	 * method.
	 */
	public Iterator<ProgressMessage> __iterator() {
		return unmodifiableListOfUnmodifiableChildren.iterator();
	}

	static class MessageThread {
		final String uid;
		MessageThread(String ownerId, Integer sequence) {
			this.uid = ownerId + "-" + (sequence == null ? "default" : ("thread-" + sequence));
		}
		MessageThread(String uid) {
			this.uid = uid;
		}
		@Override
		public String toString() {
			return uid;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + uid.hashCode();
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MessageThread other = (MessageThread)obj;
			return uid.equals(other.uid);
		}
	}
}
