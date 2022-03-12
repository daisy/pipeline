package org.daisy.pipeline.braille.common;

import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

public abstract class AbstractTransform implements Transform {
	
	private static final AtomicLong idCounter = new AtomicLong(0);
	private static final Queue<Long> availableIds = new ConcurrentLinkedQueue<>();
	
	private final long idNumber;
	private final String id;
	
	protected AbstractTransform() {
		idNumber = getUniqueId();
		id = "transform" + idNumber;
	}
	
	public String getIdentifier() {
		return id;
	}
	
	private static long getUniqueId() {
		try {
			return availableIds.remove();
		} catch (NoSuchElementException e) {
			return idCounter.incrementAndGet();
		}
	}
	
	public ToStringHelper toStringHelper() {
		return MoreObjects.toStringHelper(this);
	}
	
	@Override
	public String toString() {
		return toStringHelper().add("id", id).toString();
	}
	
	public void finalize() {
		availableIds.add(idNumber);
	}
}
