package org.daisy.common.messaging;

import java.util.List;
import java.util.Set;

import org.daisy.common.messaging.Message.Level;


/**
 * Gives access to the stored messages by level.
 */
public abstract class MessageAccessor{

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
	public abstract boolean delete();


	public abstract MessageFilter createFilter();

	public interface  MessageFilter{
		public MessageFilter filterLevels(Set<Level> levels);
		public MessageFilter greaterThan(int sequence);
		public MessageFilter inRange(int start, int end);
		public List<Message> getMessages();
	}
}
