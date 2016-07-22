package org.daisy.pipeline.webserviceutils.callback.impl;

import java.util.ArrayList;
import java.util.List;

import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.webserviceutils.callback.Callback;
import org.daisy.pipeline.webserviceutils.callback.CallbackRegistry;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultCallbackRegistry implements CallbackRegistry {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(DefaultCallbackRegistry.class);
	private List<Callback> callbacks;

	public void activate() {
		logger.debug("activated!");
	}
	
	public void init(BundleContext context) {
		callbacks = new ArrayList<Callback>();
	}

	public void close() {
	}

	@Override
	public Iterable<Callback> getCallbacks(JobId id) {
		List<Callback> filteredList = new ArrayList<Callback>();
		for (Callback callback : callbacks) {
			if (callback.getJobId().toString().equals(id.toString())) {
				filteredList.add(callback);
			}
		}
		return filteredList;
	}

	@Override
	public void addCallback(Callback callback) {
		callbacks.add(callback);
	}

	@Override
	public void removeCallback(Callback callback) {
		callbacks.remove(callback);
	}
}
