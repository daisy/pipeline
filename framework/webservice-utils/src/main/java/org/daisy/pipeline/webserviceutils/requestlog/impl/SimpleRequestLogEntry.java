package org.daisy.pipeline.webserviceutils.requestlog.impl;

import org.daisy.pipeline.webserviceutils.requestlog.RequestLogEntry;

public final class SimpleRequestLogEntry implements RequestLogEntry {

	private final String clientId;
	private final String nonce;
	private final String timestamp;

	public SimpleRequestLogEntry(String clientId, String nonce, String timestamp) {
		this.clientId = clientId;
		this.nonce = nonce;
		this.timestamp = timestamp;
	}

	@Override
	public String getClientId() {
		return clientId;
	}

	@Override
	public String getNonce() {
		return nonce;
	}

	@Override
	public String getTimestamp() {
		return timestamp;
	}

}
