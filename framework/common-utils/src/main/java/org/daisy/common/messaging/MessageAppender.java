package org.daisy.common.messaging;

import org.slf4j.Logger;

/**
 * An object that can modify an underlying message by appending child messages to it or by closing it.
 */
public interface MessageAppender extends AutoCloseable {

	/**
	 * Append a child to the underlying message.
	 *
	 * @return the appended message
	 */
	public MessageAppender append(MessageBuilder message);

	/**
	 * Close the underlying message.
	 *
	 * If a message is closed, the progress becomes 1 and no child message can be appended.
	 */
	public void close();

	/**
	 * {@link Logger} that appends messages to the underlying message.
	 */
	public default Logger asLogger() {
		return new AbstractLogger() {
			protected void logMessage(MessageBuilder msg) {
				MessageAppender.this.append(msg).close();
			}
		};
	}

	/**
	 * Returns the "active" block in the current "message thread".
	 *
	 * A message thread is a collection of messages that belong to the same {@link
	 * Message#getOwnerId() owner} ("job") and {@link Thread thread}.
	 *
	 * The active block is the portion of the process currently in progress. Whenever a new {@link
	 * MessageImpl} is created it becomes the new active block of that thread, until it is closed,
	 * and if its parent belongs to the same thread it then becomes the active block.
	 *
	 * This method assumes that different jobs are run in different threads.
	 */
	public static MessageAppender getActiveBlock() {
		return MessageImpl.activeBlock.get();
	}

	/**
	 * Returns the active block in the specified message thread.
	 *
	 * This method is intended to be used by the <code>JobProgressAppender</code> class which receives
	 * log messages with "message-thread" info attached to them through the {@link org.slf4j.MDC}
	 * mechanism.
	 */
	public static MessageAppender getActiveBlock(String threadId) {
		if (threadId == null)
			throw new IllegalArgumentException();
		return MessageImpl.activeBlockInThread.get(new MessageImpl.MessageThread(threadId));
	}

	/**
	 * {@link Logger} that appends messages to the active block at the time of the log calls.
	 */
	public static Logger getActiveBlockLogger() {
		return new AbstractLogger() {
			protected void logMessage(MessageBuilder msg) {
				MessageAppender activeBlock = MessageAppender.getActiveBlock();
				// we need an active block otherwise we have no place to send the message to
				if (activeBlock != null)
					activeBlock.append(msg).close();
			}
		};
	}
}
