package org.daisy.pipeline.tts;

import java.util.Collection;

public interface LoadBalancer {
	static class Host {
		public String address;
		public int port;

		@Override
		public String toString() {
			return address + ":" + port;
		}
	}

	/**
	 * Can be called from different threads.
	 * 
	 * @return the next host chosen according to the load balancer's strategy.
	 */
	Host selectHost();

	/**
	 * 
	 * Must not be called from different threads.
	 * 
	 * @return all the possible hosts that selectHost() can return. It may be
	 *         used for checking that all the servers are up. It may not return
	 *         the master host as long as it cannot be returned by selectHost().
	 */
	Collection<Host> getAllHosts();

	/**
	 * Can be called from different threads.
	 * 
	 * @return the host that should be used for querying general information
	 *         such as the available voices etc.
	 */
	Host getMaster();

	/**
	 * Discard the given host. After this call, selectHost() will no longer
	 * return @param h. Must not be called from different threads.
	 */
	void discard(Host h);
}
