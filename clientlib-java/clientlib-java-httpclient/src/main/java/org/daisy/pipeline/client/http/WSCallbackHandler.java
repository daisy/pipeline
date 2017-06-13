package org.daisy.pipeline.client.http;

import java.util.List;

import org.daisy.pipeline.client.models.Message;
import org.daisy.pipeline.client.models.Job.Status;


public interface WSCallbackHandler {
	
	/**
	 * Handle job message callbacks.
	 * @param jobMessages
	 */
	public void jobMessages(List<Message> jobMessages);
	
	/**
	 * Handle job status update callbacks.
	 * @param jobStatus
	 */
	public void jobStatus(Status jobStatus);
	
}
