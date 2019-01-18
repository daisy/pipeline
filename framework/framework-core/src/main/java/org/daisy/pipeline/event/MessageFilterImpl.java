package org.daisy.pipeline.event;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.MessageAccessor.MessageFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MessageFilterImpl implements MessageFilter {

	private static final Set<Level> allLevels = ImmutableSet.copyOf(Level.values());
	private static final Logger logger = LoggerFactory.getLogger(MessageFilterImpl.class);

	private final ProgressMessage message;
	private final boolean excludeSelf;
	private Filter filter = null;
	private Set<Level> levels = new HashSet<Level>(allLevels);
	private Integer start = null;
	private Integer end = null;
	private boolean onlyWithText = false;

	MessageFilterImpl(ProgressMessage message, boolean excludeSelf) {
		this.message = message;
		this.excludeSelf = excludeSelf;
	}

	public MessageFilter filterLevels(Set<Level> levels) {
		if (this.levels.size() == allLevels.size())
			this.levels = levels;
		else
			this.levels = Sets.intersection(this.levels, levels);
		filter = null;
		return this;
	}

	public MessageFilter greaterThan(int sequence) {
		if (start == null || sequence >= start) {
			start = sequence + 1;
			filter = null;
		}
		return this;
	}

	public MessageFilter inRange(int start, int end) {
		if (start < 0)
			throw new IndexOutOfBoundsException("range start has to be 0 or greater");
		if (start > end)
			throw new IllegalArgumentException("range start is greater than end");
		if (this.start == null || start > this.start) {
			this.start = start;
			filter = null;
		}
		if (this.end == null || end < this.end) {
			this.end = end;
			filter = null;
		}
		return this;
	}

	MessageFilterImpl withText() {
		if (!onlyWithText) {
			onlyWithText = true;
			filter = null;
		}
		return this;
	}

	/**
	 * Returns an immutable list of immutable messages (read-only deep copies).
	 */
	public List<Message> getMessages() {
		if (filter == null) {
			if (excludeSelf)
				filter = compose(getChildren, filter);
			if (start != null || end != null) {
				if (end == null)
					filter = compose(sequenceFilter(start), filter);
				else
					filter = compose(sequenceFilter(start, end), filter);
			}
			if (levels.size() < allLevels.size()) {
				filter = compose(levelFilter(levels), filter);
			}
			if (onlyWithText) {
				filter = compose(textFilter, filter);
			}
		}
		List<Message> r;
		synchronized(ProgressMessage.MUTEX) {
			if (filter != null)
				r = Lists.<Message>newArrayList(
					Iterators.transform(
						filter.apply(message).iterator(),
						m -> m.deepCopy()));
			else
				r = Collections.<Message>singletonList(message.deepCopy());
		}
		return r;
	}
	
	/**
	 * Promote a message
	 *
	 * @param progressFactor is the portion of the hidden parent
	 */
	private static ProgressMessage promote(ProgressMessage message, final BigDecimal progressFactor) {
		if (progressFactor.compareTo(BigDecimal.ONE) < 0)
			return new ProgressMessage.UnmodifiableProgressMessage(message) {
				@Override
				public BigDecimal getPortion() {
					return progressFactor.multiply(super.getPortion()); }};
		else
			return message;
	}

	/**
	 * Returned Iterable can not be used again after message has been modified.
	 */
	private static interface Filter extends Function<ProgressMessage,Iterable<ProgressMessage>> {}

	/** Get children */
	// (m -> m) would give us a deep copy which would be wrong
	private final static Filter getChildren = m -> () -> m._iterator();

	/** Promote children */
	private final static Filter promoteChildren = message -> {
		final BigDecimal portion = message.getPortion();
		return portion.compareTo(BigDecimal.ONE) == 0
			? getChildren.apply(message)
			: Iterables.transform(
				getChildren.apply(message),
				m -> promote(m, portion));
	};
	
	/**
	 * Create deeply filtered view of message (top down)
	 *
	 * @param shallowFilter is applied on the message itself and recursively on the children
	 */
	@SuppressWarnings("unused")
	private static Filter deepFilterTopDown(final Filter shallowFilter) {
		return new Filter() {
			private final Filter recur = compose(this, getChildren);
			public Iterable<ProgressMessage> apply(ProgressMessage message) {
				return Iterables.transform(
					shallowFilter.apply(message),
					m -> new ProgressMessage.UnmodifiableProgressMessage(m) {
						private final Iterable<ProgressMessage> children = recur.apply(m);
						@Override
						public Iterator<ProgressMessage> __iterator() {
							return children.iterator(); }});
			}
		};
	}

	/**
	 * Create deeply filtered view of message (bottom up)
	 *
	 * @param shallowFilter is applied on leaf messages and recursively on the parents
	 */
	private static Filter deepFilterBottomUp(final Filter shallowFilter) {
		return new Filter() {
			private final Filter recur = compose(this, getChildren);
			public Iterable<ProgressMessage> apply(ProgressMessage message) {
				return shallowFilter.apply(
					new ProgressMessage.UnmodifiableProgressMessage(message) {
						private final Iterable<ProgressMessage> children = recur.apply(message);
						@Override
						public Iterator<ProgressMessage> __iterator() {
							return children.iterator(); }});
			}
		};
	}
	
	private static Filter sequenceFilter(final int start) {
		return sequenceFilter(start, Integer.MAX_VALUE);
	}
	
	/**
	 * Hide messages that are not within the given range and that have no descendants within that range.
	 */
	private static Filter sequenceFilter(final int start, final int end) {
		return deepFilterBottomUp(
			m -> isLessOrEqual(start, m.getSequence(), end)
			     || (!m.isOpen() && isLessOrEqual(start, m.getCloseSequence(), end))
			     || !m.isEmpty()
				? m.selfIterate()
				: empty
		);
	}

	/**
	 * Hide messages that are not in certains levels and promote their children.
	 */
	private static Filter levelFilter(final Set<Level> levels) {
		return deepFilterBottomUp(
			m -> levels.contains(m.getLevel())
				? m.selfIterate()
				: promoteChildren.apply(m)
		);
	}

	/**
	 * Hide messages without text and promote their children.
	 */
	private static Filter textFilter = deepFilterBottomUp(
		m -> m.getText() != null
			? new ProgressMessage.UnmodifiableProgressMessage(m) {
					@Override // messages without text already filtered out, only need to make copies
					public Iterator<ProgressMessage> iterator() {
						return Iterators.transform(_iterator(), mm -> mm.deepCopy());
					}
				}.selfIterate()
			: promoteChildren.apply(m)
	);

	private static final Iterable<ProgressMessage> empty = Optional.<ProgressMessage>absent().asSet();

	/** Apply the first filter after the second */
	private static Filter compose(final Filter g, final Filter f) {
		if (f == null)
			return g;
		else if (g == null)
			return f;
		else
			return m -> Iterables.concat(
				Iterables.transform(f.apply(m), g));
	}

	@SuppressWarnings("unused")
	private static <T extends Comparable<T>> boolean isEqual(T... values) {
		T prev = null;
		for (T v : values) {
			if (prev != null && prev.compareTo(v) != 0)
				return false;
			prev = v; }
		return true;
	}

	@SuppressWarnings("unused")
	private static <T extends Comparable<T>> boolean isLess(T... values) {
		T prev = null;
		for (T v : values) {
			if (prev != null && prev.compareTo(v) >= 0)
				return false;
			prev = v; }
		return true;
	}

	private static <T extends Comparable<T>> boolean isLessOrEqual(T... values) {
		T prev = null;
		for (T v : values) {
			if (prev != null && prev.compareTo(v) > 0)
				return false;
			prev = v; }
		return true;
	}

	@SuppressWarnings("unused")
	private static <T extends Comparable<T>> boolean isGreater(T... values) {
		T prev = null;
		for (T v : values) {
			if (prev != null && prev.compareTo(v) <= 0)
				return false;
			prev = v; }
		return true;
	}
}
