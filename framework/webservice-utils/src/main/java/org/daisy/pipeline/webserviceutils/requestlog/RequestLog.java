package org.daisy.pipeline.webserviceutils.requestlog;

public interface RequestLog {
	boolean contains(RequestLogEntry entry);
	void add(RequestLogEntry entry);
}
