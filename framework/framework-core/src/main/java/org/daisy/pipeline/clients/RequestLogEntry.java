package org.daisy.pipeline.clients;

public interface RequestLogEntry {

	String getClientId();
	String getNonce();
	String getTimestamp();
}
