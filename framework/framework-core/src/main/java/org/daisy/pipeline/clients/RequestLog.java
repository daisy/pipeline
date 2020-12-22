package org.daisy.pipeline.clients;

public interface RequestLog {
	boolean contains(RequestLogEntry entry);
	void add(RequestLogEntry entry);
}
