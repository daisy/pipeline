package org.daisy.pipeline.webserviceutils.callback;

import java.net.URI;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.JobId;

public class Callback {
	public enum CallbackType {STATUS, MESSAGES}

	private final URI href;
	private final CallbackType type;
	private final JobId jobId;
	private int frequency = 1;
	private final Client client;

	public Callback(JobId jobId, Client client, URI href, CallbackType type, int frequency) {
		this.href = href;
		this.type = type;
		this.jobId = jobId;
		this.client = client;
		this.frequency = frequency;
	}
	public JobId getJobId() {
		return jobId;
	}

	public URI getHref() {
		return href;
	}

	public CallbackType getType() {
		return type;
	}

	public int getFrequency() {
		return frequency;
	}

	public Client getClient() {
		return client;
	}
}