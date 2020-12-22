package org.daisy.pipeline.webservice;

import java.net.URI;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.Job;

public class Callback {
	public enum CallbackType {STATUS, MESSAGES}

	private final URI href;
	private final CallbackType type;
	private final Job job;
	private int frequency = 1;
	private final Client client;

	public Callback(Job job, Client client, URI href, CallbackType type, int frequency) {
		this.href = href;
		this.type = type;
		this.job = job;
		this.client = client;
		this.frequency = frequency;
	}
	public Job getJob() {
		return job;
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

	@Override
	public String toString() {
		return "Callback [href='" + href+ "'; client=" + client + "]";
	}
}
