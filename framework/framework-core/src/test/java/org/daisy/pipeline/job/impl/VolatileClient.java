package org.daisy.pipeline.job.impl;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client;

public class VolatileClient implements Client {

	private String id;
	private Role role;
	private Priority priority;

	public VolatileClient(String id, Role role, Priority priority) {
		this.id = id;
		this.role = role;
		this.priority = priority;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getSecret() {
		return "";
	}

	@Override
	public String getContactInfo() {
		return "";
	}

	@Override
	public Priority getPriority() {
		return priority;
	}

	@Override
	public Role getRole() {
		return this.role;
	}
}
