package org.daisy.pipeline.braille.common;

import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

public abstract class AbstractTransform implements Transform {
	
	private final String id = "transform" + getUniqueId();
	
	public String getIdentifier() {
		return id;
	}
	
	private static AtomicLong i = new AtomicLong(0);
	
	private static long getUniqueId() {
		return i.incrementAndGet();
	}
	
	public XProc asXProc() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	public ToStringHelper toStringHelper() {
		return MoreObjects.toStringHelper(this);
	}
	
	@Override
	public String toString() {
		return toStringHelper().add("id", id).toString();
	}
}
