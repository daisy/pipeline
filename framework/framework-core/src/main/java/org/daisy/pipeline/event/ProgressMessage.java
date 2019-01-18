package org.daisy.pipeline.event;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.MessageAccessor.MessageFilter;

import org.slf4j.Logger;

/**
 * A progress message is an extension of a simple message, with a certain "progress" and with
 * possible nested (progress) messages. Every progress message represents a certain "portion" of the
 * conversion process. The size of the portion is expressed as a fraction (number between 0 and 1)
 * of the "parent" message, or of the whole process in case it is a "top-level" message (without a
 * parent). The progress is a number between 0 (not started) and 1 (completed) that indicates how
 * much of the process has been completed. The total progress of a portion is always greater than or
 * equal to the sum of the child portions' progresses (multiplied by the corresponding
 * portions). Sibling portions have no particular order. They may run sequentially or in parallel.
 */
public abstract class ProgressMessage extends Message implements MessageFilter, Iterable<ProgressMessage> {
	
	public abstract String getJobId();

	/** The total progress of this message */
	public abstract BigDecimal getProgress();
	
	/** Portion within parent (or within whole process if no parent) */
	public abstract BigDecimal getPortion();

	/** If a message is closed, the progress becomes 1 and no child message can be added. */
	public abstract void close();

	/** Add a child message. */
	public abstract ProgressMessage post(ProgressMessageBuilder message);

	/** Whether or not the close() method has been previously called */
	abstract boolean isOpen();

	/**
	 * Sequence number of an imaginary "close" message. Used by MessageFilterImpl to determine
	 * whether a message is to be included in a sequence range.
	 */
	abstract int getCloseSequence();

	private Logger asLogger;

	/** org.slf4j.Logger interface for this ProgressMessage */
	public Logger asLogger() {
		if (asLogger == null)
			asLogger = new ProgressMessageLogger(this);
		return asLogger;
	}

	private Iterator<ProgressMessage> i = null;

	/** Whether or not child messages are present. */
	final boolean isEmpty() {
		if (i == null)
			i = __iterator();
		return !i.hasNext();
	}

	final Iterator<ProgressMessage> _iterator() {
		if (i != null) {
			Iterator<ProgressMessage> ret = i;
			i = null;
			return ret;
		} else
			return __iterator();
	}

	/** No deep copies */
	abstract Iterator<ProgressMessage> __iterator();

	private Iterable<ProgressMessage> singleton = null;
	Iterable<ProgressMessage> selfIterate() {
		if (singleton == null)
			singleton = Collections.<ProgressMessage>singleton(this);
		return singleton;
	}

	/**
	 * Returns a (read-only) view that excludes text-less messages (messages that only carry
	 * progress information). Child messages of excluded messages are promoted (become direct
	 * children of the excluded message's parent).
	 */
	public MessageFilter asMessageFilter() {
		return new MessageFilterImpl(this, false).withText();
	}

	/**
	 * Returns a (read-only) view with only messages of a given severity level. Child messages
	 * of excluded messages are promoted (become direct children of the excluded message's
	 * parent).
	 */
	public MessageFilter filterLevels(Set<Level> levels) {
		return asMessageFilter().filterLevels(levels);
	}

	public MessageFilter greaterThan(int sequence) {
		return asMessageFilter().greaterThan(sequence);
	}

	/**
	 * Returns a (read-only) view with only messages with a sequence number within a given
	 * range, or with a descendant message within that range.
	 */
	public MessageFilter inRange(int start, int end) {
		return asMessageFilter().inRange(start, end);
	}

	/**
	 * If this message has text, returns an immutable version (read-only deep copy) of this it as a
	 * singleton list. If this messages has no text, returns the immutable versions of its
	 * descdendants with text.
	 */
	public List<Message> getMessages() {
		return asMessageFilter().getMessages();
	}

	/**
	 * Iterate over the child messages. The returned messages are immutable (read-only deep copies).
	 */
	@SuppressWarnings(
		"unchecked" // safe cast to Iterator<ProgressMessage>
	)
	public Iterator<ProgressMessage> iterator() {
		return (Iterator<ProgressMessage>)(Object)(new MessageFilterImpl(this, true).withText().getMessages().iterator());
	}

	@Override
	public String toString() {
		String s = "" + getSequence();
		String children = Lists.<Message>newArrayList(this).toString();
		if (!children.equals("[]"))
			s += (" " + children);
		return s;
	}

	// Package private constructor
	ProgressMessage(Throwable throwable, String text, Level level, int line,
	                int column, Date timeStamp, Integer sequence, String jobId, String file) {
		super(throwable, text, level, line, column, timeStamp, sequence, jobId, file);
	}

	/**
	 * Mutex object used for blocking modifications to a ProgressMessage while it is being
	 * iterated.
	 */
	static final Object MUTEX = new Object();

	/**
	 * Returns the "active" block in the current thread.
	 *
	 * The active block is the portion of the process currently in progress. New messages are
	 * appended as children to this block. When a new message is created it becomes the new
	 * active block until it is closed.
	 */
	public static ProgressMessage getActiveBlock() {
		return ProgressMessageImpl.activeBlock.get();
	}

	/** Returns the active block in the specified job thread */
	public static ProgressMessage getActiveBlock(String jobId, String threadId) {
		if (threadId == null)
			threadId = "default";
		return ProgressMessageImpl.activeBlockInThread.get(new ProgressMessageImpl.JobThread(jobId, threadId));
	}

	/**
	 * Whether deepCopy should return "this".
	 */
	boolean isImmutable() {
		return false;
	}

	/** Get an immutable view of the message (read-only deep copy). */
	ProgressMessage deepCopy() {
		if (isImmutable())
			return this;
		final Iterable<ProgressMessage> children = ImmutableList.copyOf(
			Iterators.transform(
				_iterator(),
				m -> m.deepCopy()));
		return new UnmodifiableProgressMessage(this) {
			@Override
			Iterator<ProgressMessage> __iterator() {
				return children.iterator();
			}
			@Override
			boolean isImmutable() {
				return true;
			}
		};
	}

	/** Get a read-only view of message */
	static ProgressMessage unmodifiable(ProgressMessage message) {
		if (message instanceof UnmodifiableProgressMessage)
			return message;
		else
			return new UnmodifiableProgressMessage(message);
	}

	static class UnmodifiableProgressMessage extends ProgressMessage {
		
		private final ProgressMessage message;
		
		UnmodifiableProgressMessage(ProgressMessage message) {
			super(message.getThrowable(),
			      message.getText(),
			      message.getLevel(),
			      message.getLine(),
			      message.getColumn(),
			      message.getTimeStamp(),
			      message.getSequence(),
			      message.getJobId(),
			      message.getFile());
			this.message = message;
		}

		public String getJobId() {
			return message.getJobId();
		}

		public BigDecimal getPortion() {
			return message.getPortion();
		}

		public BigDecimal getProgress() {
			return message.getProgress();
		}

		public void close() {
			throw new UnsupportedOperationException("This is a read-only view of the message");
		}

		public ProgressMessage post(ProgressMessageBuilder m) {
			throw new UnsupportedOperationException("This is a read-only view of the message");
		}

		boolean isOpen() {
			return message.isOpen();
		}

		int getCloseSequence() {
			return message.getCloseSequence();
		}

		// unmodifiable is not the same as immutable
		boolean isImmutable() {
			return message.isImmutable();
		}

		Iterator<ProgressMessage> __iterator() {
			return message.__iterator();
		}

		@Override
		public Iterator<ProgressMessage> iterator() {
			return message.iterator();
		}

		@Override
		public String toString() {
			return message.toString();
		}
	}
}
