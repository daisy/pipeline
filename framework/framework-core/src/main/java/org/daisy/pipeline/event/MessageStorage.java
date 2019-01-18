package org.daisy.pipeline.event;

import org.daisy.common.messaging.Message;

public interface MessageStorage {
	public boolean add(Message msg);
	public boolean remove(String jobId);
	public Iterable<Message> get(String jobId);
}
