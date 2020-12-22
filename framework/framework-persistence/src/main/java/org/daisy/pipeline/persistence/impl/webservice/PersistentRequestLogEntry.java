package org.daisy.pipeline.persistence.impl.webservice;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.daisy.pipeline.clients.RequestLogEntry;

// Use this class to record request nonces and timestamps
@Entity
//@NoSql(dataFormat=DataFormatType.MAPPED)
public class PersistentRequestLogEntry implements RequestLogEntry{

	//FIXME id should depend on the other fields
	@Id
	@GeneratedValue
	private String internalId;

	public String getInternalId() {
		return internalId;
	}

	// the fields for each request
	private String clientId;
	private String nonce;
	private String timestamp;

	public PersistentRequestLogEntry() {
	}

	public PersistentRequestLogEntry(RequestLogEntry entry) {
		this.clientId = entry.getClientId();
		this.nonce = entry.getNonce();
		this.timestamp = entry.getTimestamp();
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}	
}
