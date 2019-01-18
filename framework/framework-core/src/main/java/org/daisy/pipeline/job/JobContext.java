package org.daisy.pipeline.job;

import java.net.URI;

import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcMonitor;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.common.xproc.XProcResult;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.script.XProcScript;

public interface JobContext {

	public XProcInput getInputs() ;
	public XProcOutput getOutputs();
	public URI getLogFile() ;
	public XProcMonitor getMonitor() ;
	public XProcScript getScript();
	public JobId getId();
	public JobBatchId getBatchId();
	public JobResultSet getResults();
	public void writeResult(XProcResult result) ;
	public String getName();
	public Client getClient();

	public void setMonitor(XProcMonitor monitor);

}
