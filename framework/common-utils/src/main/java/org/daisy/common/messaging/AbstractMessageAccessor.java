package org.daisy.common.messaging;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.daisy.common.messaging.Message.Level;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public abstract class AbstractMessageAccessor extends MessageAccessor {

	private final Set<Level> allLevels;

	public AbstractMessageAccessor(Level threshold) {
		this.allLevels = new HashSet<>();
		for (Level l : Level.values())
			if (!threshold.isMoreSevereThan(l))
				allLevels.add(l);
	}

	/**
	 * Must return a (mutable) iterable that contains all messages (which are mutable) received
	 * up till now. Mutating operations on the iterable must be synchronized on the object
	 * itself.
	 */
	protected abstract Iterable<Message> allMessages();

	@Override
	public List<Message> getAll() {
		return createFilter().getMessages();
	}

	@Override
	public BigDecimal getProgress() {
		BigDecimal progress;
		synchronized (ProgressMessage.MUTEX) { // objects inside iterable are mutable
			Iterable<Message> messages = allMessages();
			synchronized (messages) { // iterable itself is mutable
				progress = StreamSupport.stream(messages.spliterator(), false).map(
					m -> {
						if (m instanceof ProgressMessage) {
							ProgressMessage pm = (ProgressMessage)m;
							return pm.getProgress().multiply(pm.getPortion()); }
						else
							return BigDecimal.ZERO; }
				).reduce(
					BigDecimal.ZERO,
					(d1, d2) -> d1.add(d2)
				).min(BigDecimal.ONE);
			}
		}
		return progress;
	}

	@Override
	public MessageFilter createFilter() {
		return new MessageFilterImpl();
	}

	private class MessageFilterImpl implements MessageFilter {

		private Set<Level> levels = new HashSet<Level>(allLevels);
		private Integer rangeStart = null;
		private Integer rangeEnd = null;
		private Integer greaterThan = null;

		@Override
		public MessageFilter filterLevels(final Set<Level> levels) {
			this.levels = Sets.intersection(this.levels, levels);
			return this;
		}

		@Override
		public MessageFilter greaterThan(int idx) {
			if (greaterThan == null || idx > greaterThan)
				greaterThan = idx;
			return this;
		}

		/**
		 * @param start inclusive
		 * @param end inclusive
		 * @return
		 */
		@Override
		public MessageFilter inRange(int start, int end) {
			if (start < 0)
				throw new IndexOutOfBoundsException("range start has to be 0 or greater");
			if (start > end)
				throw new IllegalArgumentException("range start is greater than end");
			if (rangeStart == null || start > rangeStart)
				rangeStart = start;
			if (rangeEnd == null || end < rangeEnd)
				rangeEnd = end;
			return this;
		}

		@Override
		public List<Message> getMessages() {
			synchronized (ProgressMessage.MUTEX) { // objects inside iterable are mutable
				Iterable<Message> messages = allMessages();
				synchronized (messages) { // iterable itself is mutable
					return Lists.newArrayList(
						Iterables.concat(
							Iterables.transform(
								messages,
								(Message m) -> {
									if (m instanceof ProgressMessage) {
										MessageFilter f = (ProgressMessage)m;
										if (greaterThan != null)
											f = f.greaterThan(greaterThan);
										if (rangeStart != null)
											f = f.inRange(rangeStart, rangeEnd);
										if (levels.size() < Level.values().length)
											f = f.filterLevels(levels);
										return f.getMessages();
									} else {
										int seq = m.getSequence();
										if ((greaterThan == null || seq > greaterThan)
										    && (rangeStart == null || (rangeStart >= seq && seq >= rangeEnd))
										    && (levels.size() == Level.values().length || levels.contains(m.getLevel())))
											return Collections.singleton(m);
										else
											return ImmutableSet.of(); }})));
				}
			}
		}
	}
}
