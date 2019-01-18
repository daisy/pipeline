package org.daisy.common.messaging;

import java.math.BigDecimal;
import java.util.function.BiConsumer;
import java.util.List;
import java.util.Set;

import org.daisy.common.messaging.Message.Level;

/**
 * Gives access to the stored messages.
 */
public abstract class MessageAccessor {

	/**
	 * Gets the errors.
	 *
	 * @return the error messages
	 */
	public List<Message> getErrors() {
		return getMessagesFrom(Level.ERROR);
	}

	/**
	 * Gets the warnings.
	 *
	 * @return the warning messages
	 */
	public List<Message> getWarnings(){
		return getMessagesFrom(Level.WARNING);
	}

	/**
	 * Gets the infos.
	 *
	 * @return the info messages
	 */
	public List<Message> getInfos(){
		return getMessagesFrom(Level.INFO);
	};

	/**
	 * Gets the debugs.
	 *
	 * @return the debug messages
	 */
	public List<Message> getDebugs(){
		return getMessagesFrom(Level.DEBUG);
	}

	/**
	 * Gets the traces.
	 *
	 * @return the trace messages
	 */
	public List<Message> getTraces(){
		return getMessagesFrom(Level.TRACE);
	}

	/**
	 * Gets the messgages from a set of levels
	 *
	 * @param fromLevels levels
	 * @return the messages
	 */
	public abstract List<Message> getAll();
	protected abstract List<Message> getMessagesFrom(Level level);

	/**
	 * Register a callback that is called whenever a new (top-level) message arrives, a message is
	 * updated with descendant messages, or the progress of a message changes.
	 *
	 * The argument must be a function that accepts a MessageAccessor, which is always this object,
	 * and a sequence number representing the event. This sequence number can then be used to get
	 * the list of all messages affected by the change, via createFilter().inRange(...).getMessages().
	 *
	 * A sequence number is never lower than the sequence numbers previously received, but the same
	 * sequence number may occur more than once.
	 */
	public abstract void listen(BiConsumer<MessageAccessor,Integer> callback);
	public abstract void unlisten(BiConsumer<MessageAccessor,Integer> callback);

	public abstract BigDecimal getProgress();

	public abstract MessageFilter createFilter();

	public interface MessageFilter {
		public MessageFilter filterLevels(Set<Level> levels);
		public MessageFilter greaterThan(int sequence);
		/**
		 * Get all the top-level messages affected by the events between "start" en "end". A
		 * top-level message may have been added, or a message may have been changed by a progress
		 * update or a new child message, or by a change of any of its already contained child
		 * messages.
		 */
		public MessageFilter inRange(int start, int end);
		public List<Message> getMessages();
	}
}
