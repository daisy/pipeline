package org.daisy.pipeline.clients;

import org.daisy.common.priority.Priority;

public interface Client {

	public enum Role {
		ADMIN,
		CLIENTAPP
	}

	public String getId();

	public String getSecret();

	public Role getRole();

	public String getContactInfo();

	public Priority getPriority();

	/**
	 * Default admin used for "non client-aware" use of the JobManagerFactory API.
	 */
	public final static Client DEFAULT_ADMIN = new Client() {
		public String getId() { return "DEFAULT_JOB_MANAGER_FACTORY_CLIENT"; }
		public String getSecret() { return ""; }
		public Role getRole() { return Client.Role.ADMIN; }
		public String getContactInfo() { return ""; }
		public Priority getPriority() { return Priority.MEDIUM; }
	};
}
