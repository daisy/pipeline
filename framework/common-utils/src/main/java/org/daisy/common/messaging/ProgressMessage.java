package org.daisy.common.messaging;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import org.daisy.common.messaging.MessageAccessor.MessageFilter;

/**
 * A progress message is an extension of a simple message, with a certain "progress" and with
 * possible nested (progress) messages. Every progress message represents a certain "portion" of the
 * conversion process. The size of the portion is expressed as a fraction (number between 0 and 1)
 * of the "parent" message, or of the whole process in case it is a "top-level" message (without a
 * parent). The progress is a number between 0 (not started) and 1 (completed) that indicates how
 * much of the process has been completed. The total progress of a portion is always greater than or
 * equal to the sum of the child portions' progresses (multiplied by the corresponding
 * portions). Sibling portions have no particular order. They may run sequentially or in parallel.
 *
 * <code>ProgressMessage</code> objects can only be created using {@link MessageBuilder}.
 */
public abstract class ProgressMessage extends Message implements MessageFilter, Iterable<ProgressMessage> {

	/* =============================================================== */
	/*                             PUBLIC                              */
	/* =============================================================== */

	/**
	 * The total progress of this message
	 */
	public abstract BigDecimal getProgress();

	/**
	 * Portion within parent (or within whole process if no parent)
	 */
	public abstract BigDecimal getPortion();

	@Override
	public String toString() {
		String s = "" + getSequence();
		String children = Lists.<Message>newArrayList(this).toString();
		if (!children.equals("[]"))
			s += (" " + children);
		return s;
	}

	/* =============================================================== */
	/*                            Iterable                             */
	/* =============================================================== */

	/**
	 * Returns a momentary view of the child messages.
	 *
	 * The messages in this iterator are immutable copies, i.e. they represent the state of the
	 * messages at the moment of this call. Their progress will not change.
	 *
	 * Messages without text (messages that carry only progress information) are replaced by their
	 * children.
	 */
	@SuppressWarnings(
		"unchecked" // safe cast to Iterator<ProgressMessage>
	)
	public Iterator<ProgressMessage> iterator() {
		return (Iterator<ProgressMessage>)(Object)(asMessageFilter(true).getMessages().iterator());
	}

	/* =============================================================== */
	/*                       MessageFilter API                         */
	/* =============================================================== */

	/**
	 * Returns a view with only messages with text and of a given severity level. Child messages of
	 * excluded messages are promoted (become direct children of the excluded message's parent).
	 */
	public MessageFilter filterLevels(Set<Level> levels) {
		return asMessageFilter(false).filterLevels(levels);
	}

	/**
	 * Returns a view with only messages with text and with a sequence number above a given
	 * threshold, or with a descendant message above that threshold.
	 */
	public MessageFilter greaterThan(int sequence) {
		return asMessageFilter(false).greaterThan(sequence);
	}

	/**
	 * Returns a view with only messages with text and with a sequence number within a given range,
	 * or with a descendant message within that range.
	 */
	public MessageFilter inRange(int start, int end) {
		return asMessageFilter(false).inRange(start, end);
	}

	/**
	 * If this message has text, returns a singleton list with an immutable copy of the message. If
	 * this message has no text, returns a list of immutable copies of its descendants with text.
	 */
	public List<Message> getMessages() {
		return asMessageFilter(false).getMessages();
	}

	/* =============================================================== */
	/*                             PRIVATE                             */
	/* =============================================================== */

	/**
	 * Whether or not this object may receive more child messages or its progress may change
	 * (whether the {@link MessageImpl#close()} method has been previously called).
	 */
	abstract boolean isOpen();

	/**
	 * Sequence number of an imaginary "close" message. Used by {@link MessageFilterImpl} to
	 * determine whether a message is to be included in a sequence range.
	 */
	abstract int getCloseSequence();

	/**
	 * Returns a view that excludes text-less messages. Child messages of excluded messages are
	 * promoted (become direct children of the excluded message's parent).
	 */
	private MessageFilter asMessageFilter(boolean excludeSelf) {
		return new MessageFilterImpl(this, excludeSelf).withText().skipRepeated(5);
	}

	private Iterator<ProgressMessage> i = null;

	/**
	 * Whether or not child messages are present.
	 */
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

	/**
	 * Iterator that does not make deep copies.
	 */
	abstract Iterator<ProgressMessage> __iterator();

	private Iterable<ProgressMessage> singleton = null;
	Iterable<ProgressMessage> selfIterate() {
		if (singleton == null)
			singleton = Collections.<ProgressMessage>singleton(this);
		return singleton;
	}

	/**
	 * Package private constructor
	 */
	ProgressMessage(Throwable throwable, String text, Level level, int line,
	                int column, Date timeStamp, Integer sequence, String ownerId, String file) {
		super(throwable, text, level, line, column, timeStamp, sequence, ownerId, file);
	}

	/**
	 * Mutex object used for blocking modifications to a ProgressMessage while it is being iterated.
	 */
	static final Object MUTEX = new Object();

	/**
	 * Whether {@link #deepCopy()} should return "this".
	 */
	boolean isImmutable() {
		return !isOpen();
	}

	/**
	 * Get an immutable view of the message.
	 */
	final ProgressMessage deepCopy() {
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

	/**
	 * Get a read-only view of a message.
	 */
	static ProgressMessage unmodifiable(ProgressMessage message) {
		if (message instanceof UnmodifiableProgressMessage)
			return message;
		else
			return new UnmodifiableProgressMessage(message);
	}

	/* The purpose of this class is to make it impossible to modify the ProgressMessage that it
	 * wraps and that also implements MessageAppender. It also provides a way to easily create
	 * ProgressMessage objects that override certain methods of a wrapped ProgressMessage (see
	 * {@link MessageFilterImpl}).
	 */
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
			      message.getOwnerId(),
			      message.getFile());
			this.message = message;
		}

		public BigDecimal getPortion() {
			return message.getPortion();
		}

		public BigDecimal getProgress() {
			return message.getProgress();
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
