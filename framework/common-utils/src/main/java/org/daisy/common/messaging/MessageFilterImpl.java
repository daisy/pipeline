package org.daisy.common.messaging;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.MessageAccessor.MessageFilter;

class MessageFilterImpl implements MessageFilter {

	private static final Set<Level> allLevels = ImmutableSet.copyOf(Level.values());

	private final ProgressMessage message;
	private final boolean excludeSelf;
	private Filter filter = null;
	private Set<Level> levels = new HashSet<Level>(allLevels);
	private Integer start = null;
	private Integer end = null;
	private boolean onlyWithText = false;
	private Integer maxRepeat = null;

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

	MessageFilterImpl skipRepeated(int maxRepeat) {
		if (maxRepeat < 2)
			throw new IllegalArgumentException();
		if (this.maxRepeat == null || maxRepeat < this.maxRepeat) {
			this.maxRepeat = maxRepeat;
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
			Filter repetitionFilter = maxRepeat != null ? repetitionFilter(maxRepeat) : null;
			if (start != null) {
				filter = compose(end == null
				                     ? sequenceFilter(start, repetitionFilter)
				                     : sequenceFilter(start, end, repetitionFilter),
				                 filter);
			} else if (repetitionFilter != null)
				filter = compose(repetitionFilter, filter);
			if (levels.size() < allLevels.size()) {
				filter = compose(levelFilter(levels), filter);
			}
			if (onlyWithText) {
				filter = compose(textFilter, filter);
			}
		}
		List<Message> r;
		synchronized (ProgressMessage.MUTEX) {
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
	
	private static Filter sequenceFilter(final int start, Filter repetitionFilter) {
		return sequenceFilter(start, Integer.MAX_VALUE, repetitionFilter);
	}
	
	/**
	 * Hide messages that are not within the given range and that have no descendants within that range.
	 */
	private static Filter sequenceFilter(final int start, final int end, Filter repetitionFilter) {
		// the main filter (applied last)
		Filter sequenceFilter = deepFilterBottomUp(
			m -> isLessOrEqual(start, m.getSequence(), end)
			     || (!m.isOpen() && isLessOrEqual(start, m.getCloseSequence(), end))
			     || !m.isEmpty()
				? m.selfIterate()
				: empty);
		if (repetitionFilter != null)
			// repetitionFilter needs to be applied before sequenceFilter because all repetitions
			// need to be count, also the hidden ones
			sequenceFilter = compose(sequenceFilter, repetitionFilter);
		// apply a pre-filter that uses the fact that a closed message which falls out of the range
		// completely (also the closing message) can not have descendant messages that fall in the
		// range
		return compose(
			sequenceFilter,
			deepFilterTopDown(
				m -> m.isOpen()
				     || (isLessOrEqual(m.getSequence(), end) && isLessOrEqual(start, m.getCloseSequence()))
					? m.selfIterate()
					: repetitionFilter != null
						// don't hide messages unless all their siblings are hidden too, so that
						// repetitionFilter can count all repetitions
						? new ProgressMessage.UnmodifiableProgressMessage(m) {
								public Iterator<ProgressMessage> __iterator() {
									return emptyIterator; }
							}.selfIterate()
						: empty));
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
	 * Hide repeated messages (sibling messages with the same text) as of the <code>maxRepeat +
	 * 1</code>'th repetition, and announce this in the <code>maxRepeat</code>'th repetition.
	 *
	 * Note that this does not affect top level messages because the counting is done within
	 * messages.
	 */
	private static Filter repetitionFilter(int maxRepeated) {
		return deepFilterBottomUp(
			m -> new ProgressMessage.UnmodifiableProgressMessage(m) {
					@Override
					public Iterator<ProgressMessage> __iterator() {
						return new AbstractIterator<ProgressMessage>() {
							Map<String,Integer> repetitions = null;
							Iterator<ProgressMessage> i = null;
							protected ProgressMessage computeNext() {
								if (i == null)
									i = m.__iterator();
								if (!i.hasNext())
									return endOfData();
								ProgressMessage mm = i.next();
								String text = mm.getText();
								if (text != null) {
									Integer repeated;
									if (repetitions == null) {
										repetitions = new HashMap<>();
										repeated = 1;
									} else {
										repeated = repetitions.get(text);
										if (repeated == null)
											repeated = 1;
										else {
											repeated++;
											if (repeated > maxRepeated)
												// make the message empty so that it will be hidden later by textFilter
												mm = new ProgressMessage.UnmodifiableProgressMessage(mm) {
													public String getText() {
														return null; }};
											else if (repeated == maxRepeated)
												mm = new ProgressMessage.UnmodifiableProgressMessage(mm) {
													public String getText() {
														return text + " (further repetitions of this message will be omitted)"; }};
										}
									}
									repetitions.put(text, repeated);
								}
								return mm;
							}
						};
					}
				}.selfIterate()
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
						synchronized (MUTEX) {
							return Lists.<ProgressMessage>newArrayList(Iterators.transform(_iterator(), mm -> mm.deepCopy())).iterator();
						}
					}
				}.selfIterate()
			: promoteChildren.apply(m)
	);

	private static final Iterable<ProgressMessage> empty = Optional.<ProgressMessage>absent().asSet();
	private static final Iterator<ProgressMessage> emptyIterator = Collections.<ProgressMessage>emptyIterator();

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
