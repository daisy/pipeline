package org.daisy.pipeline.webservice.restlet;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.WebserviceStorage;
import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.webservice.CallbackHandler;
import org.daisy.pipeline.webservice.PipelineWebServiceConfiguration;
import org.daisy.pipeline.webservice.Properties;
import org.daisy.pipeline.webservice.restlet.impl.PipelineWebService;
import org.daisy.pipeline.webservice.xml.ErrorWriter;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.data.Warning;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.Request;
import org.restlet.resource.ServerResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenericResource extends ServerResource {

	private static Logger logger = LoggerFactory.getLogger(GenericResource.class);

	public PipelineWebServiceConfiguration getConfiguration() {
		return webservice().getConfiguration();
	}

	public WebserviceStorage getStorage() {
		return webservice().getStorage();
	}

	public JobManager getJobManager(Client client) {
		return webservice().getJobManager(client);
	}

	public JobManager getJobManager(Client client, JobBatchId batchId) {
		return webservice().getJobManager(client, batchId);
	}

	public ScriptRegistry getScriptRegistry() {
		return webservice().getScriptRegistry();
	}

	public DatatypeRegistry getDatatypeRegistry() {
		return webservice().getDatatypeRegistry();
	}

	public CallbackHandler getCallbackHandler() {
		return webservice().getCallbackHandler();
	}

	public boolean shutDown(long key) {
		return webservice().shutDown(key);
	}

	private PipelineWebService webservice() {
		return (PipelineWebService)getApplication();
	}

	protected Reference getWebSocketRootRef() {
		Reference websocketRootRef = new Reference(getRequest().getRootRef());
		websocketRootRef.setScheme("ws");
		websocketRootRef.setHostPort(webservice().getWebSocketPort());
		return websocketRootRef;
	}

	protected Representation getErrorRepresentation(Throwable error) {
		logger.debug("Error in fulfilling request:", error);
		ErrorWriter.ErrorWriterBuilder builder = new ErrorWriter.ErrorWriterBuilder()
		                                                        .withError(error)
		                                                        .withUri(this.getStatus().getUri());
		DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, builder.build().getXmlDocument());
		logResponse(dom);
		return dom;
	}

	// Only use this method when there is no stack trace information. Use the
	// getErrorRepresentation(Throwable) method if possible, so that the stack trace is included in
	// the logs. ErrorWriter does not include the stack trace in the XML representation anyway.
	protected Representation getErrorRepresentation(String error) {
		logger.debug("Error in fulfilling request: " + error);
		ErrorWriter.ErrorWriterBuilder builder = new ErrorWriter.ErrorWriterBuilder()
		                                                        .withError(new Throwable(error))
		                                                        .withUri(this.getStatus().getUri());
		DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, builder.build().getXmlDocument());
		logResponse(dom);
		return dom;
	}
	
	@Override
	public void doCatch(Throwable e) {
		logger.debug(null, e);
		super.doCatch(e);
	}
	
	@Override
	public void setStatus(Status status) {
		logStatus(status);
		super.setStatus(status);
	}
	
	protected void logResponse(DomRepresentation dom) {
		if (logger.isDebugEnabled())
			try {
				StringWriter w = new StringWriter();
				dom.write(w);
				logger.debug(w.getBuffer().toString());
			} catch (java.io.IOException e) {}
	}
	
	protected void logRequest() {
		Request req = getRequest();
		logger.debug(req.getMethod()+" "+req.getResourceRef().getPath());
	}

	private void logStatus(Status status) {
		logger.debug(status.toString());
	}

	protected void addWarningHeader(int code, String description) {
		addWarningHeader(code, "-", description);
	}

	protected void addWarningHeader(int code, String agent, String description) {
		List<Warning> warnings = getResponse().getWarnings();
		if (warnings == null) {
			warnings = new ArrayList<Warning>();
			getResponse().setWarnings(warnings);
		}
		Warning w = new Warning();
		w.setStatus(new Status(code));
		w.setAgent(agent);
		w.setText(description);
		warnings.add(w);
	}

	private static boolean shouldEnableCORS = Properties.CORS.get("false").equalsIgnoreCase("true");

	protected void maybeEnableCORS() {
		if (shouldEnableCORS)
			enableCORS("*");
	}

	private void enableCORS(String domain) {
		getResponse().setAccessControlAllowOrigin(domain);
	}
}
