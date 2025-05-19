package org.daisy.pipeline.webservice.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.google.common.base.Optional;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.job.JobManagerFactory;
import org.daisy.pipeline.webservice.Callback;
import org.daisy.pipeline.webservice.Callback.CallbackType;
import org.daisy.pipeline.webservice.CallbackHandler;
import org.daisy.pipeline.webservice.Routes;
import org.daisy.pipeline.webservice.impl.AuthenticationFilter.ClientPrincipal;

import org.restlet.data.Form;
import org.restlet.data.Reference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(Routes.JOB_ROUTE)
public class JobsWebSocketEndpoint {

	private static final Logger logger = LoggerFactory.getLogger(JobsWebSocketEndpoint.class);

	private final CallbackHandler callbackHandler;
	private final JobManagerFactory jobManagerFactory;
	private final Routes routes;
	private final Map<String,Callback> callbacks = Collections.synchronizedMap(new HashMap<String,Callback>());

	public JobsWebSocketEndpoint(JobManagerFactory jobManagerFactory,
	                             CallbackHandler callbackHandler,
	                             Routes routes) {
		this.jobManagerFactory = jobManagerFactory;
		this.callbackHandler = callbackHandler;
		if (callbackHandler == null)
			throw new IllegalArgumentException("no push notifier");
		this.routes = routes;
	}

	@OnOpen
	public void onOpen(@PathParam("id") String jobId, Session session) {
		logger.debug("Opening web socket session " + session.getId() + ": " + session.getRequestURI().getPath());
		Client client = ((ClientPrincipal)session.getUserPrincipal()).getClient();
		//            = (Client)session.getUserProperties().get(Client.class.getName());
		JobManager jobManager = jobManagerFactory.createFor(client);
		Optional<Job> job = jobManager.getJob(JobIdFactory.newIdFromString(jobId));
		if (!job.isPresent())
			// this wouldn't happen if the code in modifyHandshake() would work
			throw new RuntimeException("No job with ID " + jobId);
		Form query = new Reference(session.getRequestURI()).getQueryAsForm();
		int firstMessage = 0; {
			String msgSeq = query.getFirstValue("msgSeq");
			if (msgSeq != null)
				try {
					firstMessage = Integer.parseInt(msgSeq) + 1;
				} catch (NumberFormatException e) {
					logger.warn("Expected integer for msgSeq parameter but got: " + msgSeq);
				}}
		CallbackType callbackType = CallbackType.MESSAGES; {
			String type = query.getFirstValue("type");
			if ("status".equals(type))
				callbackType = CallbackType.STATUS;
			else if ("progress".equals(type))
				callbackType = CallbackType.PROGRESS;}
		Callback callback = new WebSocketCallback(
			job.get(), callbackType, 1, firstMessage, session, routes);
		callbacks.put(session.getId(), callback);
		callbackHandler.addCallback(callback);
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		logger.debug("Received message in session " + session.getId() + ": " + message);
	}

	@OnError
	public void onError(Throwable e, Session session) {
		logger.error("Error happened in session " + session.getId(), e);
	}

	@OnClose
	public void onClose(Session session) {
		logger.debug("Closing session " + session.getId());
		Callback callback = callbacks.remove(session.getId());
		if (callback != null)
			callbackHandler.removeCallback(callback);
	}
}
