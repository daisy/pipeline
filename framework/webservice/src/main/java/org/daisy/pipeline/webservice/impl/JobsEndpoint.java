package org.daisy.pipeline.webservice.impl;

import java.io.IOException;
import java.math.BigDecimal;
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
import org.daisy.pipeline.webservice.Authenticator;
import org.daisy.pipeline.webservice.Callback;
import org.daisy.pipeline.webservice.Callback.CallbackType;
import org.daisy.pipeline.webservice.CallbackHandler;
import org.daisy.pipeline.webservice.Routes;

import org.restlet.data.Form;
import org.restlet.data.Reference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint(Routes.JOB_ROUTE)
public class JobsEndpoint {

	private static final Logger logger = LoggerFactory.getLogger(JobsEndpoint.class);

	private final PipelineWebService webservice;
	private final CallbackHandler callbackHandler;
	private final Map<String,Callback> callbacks = Collections.synchronizedMap(new HashMap<String,Callback>());

	public JobsEndpoint(PipelineWebService webservice) {
		this.webservice = webservice;
		this.callbackHandler = webservice.getCallbackHandler();
		if (callbackHandler == null)
			throw new IllegalArgumentException("no push notifier");
	}

	@OnOpen
	public void onOpen(@PathParam("id") String jobId, Session session) {
		logger.debug("Opening session " + session.getId() + ": " + session.getRequestURI().getPath());
		Client client = AuthenticatedResource.authenticate(
			webservice,
			session.getRequestURI().toString(),
			new Reference(session.getRequestURI()).getQueryAsForm());
		if (client == null) {
			throw new RuntimeException("authentication error");
		}
		JobManager jobManager = webservice.getJobManager(client);
		Optional<Job> job = jobManager.getJob(JobIdFactory.newIdFromString(jobId));

		if (!job.isPresent()) {
			throw new RuntimeException("no such job");
		}

		Form query = new Reference(session.getRequestURI()).getQueryAsForm();
		int firstMessage = 0; {
			String msgSeq = query.getFirstValue("msgSeq");
			if (msgSeq != null)
				try {
					firstMessage = Integer.parseInt(msgSeq) + 1;
				} catch (NumberFormatException e) {
					logger.warn("Expected integer for msgSeq parameter but got: " + msgSeq);
				}}
		Callback callback = new WebSocketCallback(job.get(), CallbackType.MESSAGES, 1, firstMessage, session);
		callbacks.put(session.getId(), callback);
		callbackHandler.addCallback(callback);
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		System.out.printf("Received message in session " + session.getId() + ": " + message);
		
		//session.getBasicRemote()
		
		
		try {
			session.getBasicRemote().sendText(String.format("We received your message: %s%n", message));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@OnError
	public void onError(Throwable e) {
		e.printStackTrace();
	}

	@OnClose
	public void onClose(Session session) {
		logger.debug("Closing session " + session.getId());
		Callback callback = callbacks.remove(session.getId());
		if (callback != null)
			callbackHandler.removeCallback(callback);
	}
}
