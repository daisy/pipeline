package org.daisy.pipeline.webservice;

import org.daisy.pipeline.job.JobId;

public interface CallbackHandler {

	public void addCallback(Callback callback);
	public void removeCallback(Callback callback);
	
}
