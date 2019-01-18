package org.daisy.pipeline.client.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/*
 * Provide sequential view of messages and detailed progress info.
 */
class JobMessages extends AbstractList<Message> {
	
	// map from sequence number to message
	private final Map<Integer,Message> messageIndex = new HashMap<Integer,Message>();
	
	// messages as a tree
	private final List<Integer> messagesTree = new ArrayList<Integer>();
	final int msgSeq;
	
	// messages as a sequence
	private final List<Integer> messagesSequence = new ArrayList<Integer>();
	
	// time stamp of the first progress message
	private Long jobStartTime = null;
	
	/**
	 * Get all messages from messagesTree newer than msgSeq as a sequence instead of a tree structure.
	 */
	public JobMessages(List<Message> messagesTree, int msgSeq) {
		for (Message m : messagesTree) {
			this.messagesTree.add(m.sequence);
			messageIndex.put(m.sequence, m);
		}
		this.msgSeq = msgSeq;
		addTree(this, messagesTree, msgSeq);
		indexTree(messageIndex, messagesTree);
	}
	
	/* Recursively add messages newer than a given number and remove existing messages with the same sequence number */
	private void addTree(List<Message> collect, Iterable<Message> messages, int newerThan) {
		for (Message m : messages) {
			if (m.sequence > newerThan) {
				Message old = messageIndex.get(m.sequence);
				if (old != null) {
					collect.remove(old);
				}
				collect.add(m);
			}
			addTree(collect, m, newerThan);
		}
	}
	
	/* Recursively add messages to index */
	private static void indexTree(Map<Integer,Message> index, Iterable<Message> messages) {
		for (Message m : messages) {
			index.put(m.sequence, m);
			indexTree(index, m);
		}
	}
	
	public Message get(int index) {
		return messageIndex.get(messagesSequence.get(index));
	}

	public int size() {
		return messagesSequence.size();
	}
	
	@Override
	public void add(int index, Message element) {
		if (element == null) {
			throw new NullPointerException();
		}
		messagesSequence.add(index, element.sequence);
		messageIndex.put(element.sequence, element);
		dirty = dirty || index < lastMessageCount;
		modCount++;
		if (element.getProgressInfo() != null) {
			if (jobStartTime == null || element.timeStamp < jobStartTime) {
				jobStartTime = element.timeStamp;
			}
		}
	}
	
	@Override
	public Message set(int index, Message element) {
		if (element == null) {
			throw new NullPointerException();
		}
		Message prev = messageIndex.get(messagesSequence.set(index, element.sequence));
		messageIndex.put(element.sequence, element);
		dirty = dirty || (index < lastMessageCount && !element.equals(prev));
		modCount++;
		return prev;
	}
	
	@Override
	public Message remove(int index) {
		dirty = dirty || index < lastMessageCount;
		modCount++;
		return messageIndex.get(messagesSequence.remove(index));
	}
	
	/**
	 * Update with a list of new messages.
	 */
	public void join(JobMessages messages) {
		Iterable<Message> tree = messages.asTree();
		for (Message m : tree) {
			Message old = messageIndex.get(m.sequence);
			if (old != null) {
				m.join(old);
			} else {
				messagesTree.add(m.sequence);
			}
		}
		addTree(this, tree, msgSeq);
		indexTree(messageIndex, tree);
	}
	
	public Iterable<Message> asTree() {
		return new Iterable<Message>() {
			public Iterator<Message> iterator() {
				Iterator<Integer> i = messagesTree.iterator();
				return new Iterator<Message>() {
					public boolean hasNext() {
						return i.hasNext();
					}
					public Message next() {
						return messageIndex.get(i.next());
					}
				};
			}
		};
	}
	
	/**
	 * Get the time when the first progress information from the server was received.
	 */
	public Long getJobStartTime() {
		return jobStartTime;
	}
	
	// cached values
	private BigDecimal progressFrom = BigDecimal.ZERO;
	private Long progressFromTime = null;
	private BigDecimal progressInterval = BigDecimal.ZERO;
	
	// dirty state
	private int lastMessageCount = 0;
	private boolean dirty = true;
	
	/**
	 * Get the job progress.
	 *
	 * This represents the most up to date progress information from the server. The
	 * progress is computed based on the messages present, if messages were skipped using
	 * msgSeq, the total progress can not be computed correctly.
	 *
	 * Only used for testing because the total progress is normally available in the
	 * `progress` attribute on the `messages` element.
	 */
	public BigDecimal getProgressFrom() {
		updateProgress();
		return progressFrom;
	}
	
	private BigDecimal computeProgressFrom() {
		return messagesTree
			.stream()
			.map(i -> messageIndex.get(i))
			.map(Message::getProgressInfo)
			.filter(o -> o != null)
			.map(p -> p.portion.multiply(p.progress))
			.reduce(
				BigDecimal.ZERO,
				(d1, d2) -> d1.add(d2))
			.min(BigDecimal.ONE);
	}
	
	/**
	 * Get the time when the most up to date progress information from the server was received.
	 */
	public Long getProgressFromTime() {
		updateProgress();
		return progressFromTime;
	}
	
	private Long computeProgressFromTime() {
		try {
			return messagesTree.stream()
			                   .map(i -> messageIndex.get(i))
			                   .filter(m -> m.progressInfo != null)
			                   .mapToLong(m -> m.timeStamp)
			                   .max()
			                   .getAsLong();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	
	/**
	 * Progress interval in which no updates from the server are expected.
	 *
	 * Not expected does not mean guaranteed not to happen.
	 */
	public BigDecimal getProgressInterval() {
		updateProgress();
		return progressInterval;
	}
	
	public BigDecimal computeProgressInterval() {
		List<BigDecimal> uncompletedMessages = new ArrayList<BigDecimal>();
		getUncompletedMessages(uncompletedMessages, asTree());
		return uncompletedMessages
			.stream()
			.reduce((d1, d2) -> d1.min(d2))
			.orElse(BigDecimal.ZERO);
	}
	
	private static void getUncompletedMessages(List<BigDecimal> collect, Iterable<Message> messages) {
		for (Message message : messages) {
			Message.ProgressInfo progress = message.getProgressInfo();
			if (progress != null && progress.progress.compareTo(BigDecimal.ONE) < 0) {
				List<BigDecimal> uncompletedChildren = new ArrayList<BigDecimal>();
				getUncompletedMessages(uncompletedChildren, message);
				if (uncompletedChildren.isEmpty()) {
					collect.add(progress.portion.multiply(BigDecimal.ONE.subtract(progress.progress)));
				} else {
					for (BigDecimal p : uncompletedChildren) {
						collect.add(p.multiply(progress.portion));
					}
				}
			}
		}
	}
	
	/* Update state if messages have been added or modified */
	private void updateProgress() {
		if (!dirty && lastMessageCount == size()) {
			return;
		}
		progressFrom = computeProgressFrom();
		progressFromTime = computeProgressFromTime();
		progressInterval = computeProgressInterval();
		lastMessageCount = size();
		dirty = false;
	}
}
