package org.daisy.pipeline.job;

import java.net.URI;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.script.XProcScript;

public interface JobContext {

	public URI getLogFile() ;
	public JobMonitor getMonitor() ;
	public XProcScript getScript();
	public JobId getId();
	public JobBatchId getBatchId();
	public JobResultSet getResults();
	public String getName();
	public Client getClient();

}
