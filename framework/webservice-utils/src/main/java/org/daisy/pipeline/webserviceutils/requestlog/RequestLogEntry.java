package org.daisy.pipeline.webserviceutils.requestlog;

public interface RequestLogEntry {

	String getClientId();
	String getNonce();
	String getTimestamp();
}
