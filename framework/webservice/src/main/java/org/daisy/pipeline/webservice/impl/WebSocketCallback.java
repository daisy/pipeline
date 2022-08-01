package org.daisy.pipeline.webservice.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.websocket.Session;

import org.daisy.common.messaging.Message;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.Job.Status;
import org.daisy.pipeline.webservice.Callback;
import org.daisy.pipeline.webservice.Callback.CallbackType;
import org.daisy.pipeline.webservice.Routes;
import org.daisy.pipeline.webservice.xml.JobXmlWriter;
import org.daisy.pipeline.webservice.xml.XmlUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;

/**
 * Callback that posts notification messages on a web socket.
 */
public class WebSocketCallback extends Callback {

	private static Logger logger = LoggerFactory.getLogger(WebSocketCallback.class.getName());

	private final Session socket;
	private final String uri;

	public WebSocketCallback(Job job, CallbackType type, int frequency, int firstMessage, Session socket, Routes routes) {
		super(job, type, frequency, firstMessage);
		this.socket = socket;
		try {
			uri = new URI(
				"http", null, socket.getRequestURI().getHost(), routes.getPort(), routes.getPath(), null, null
			).toString();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean postMessages(List<Message> messages, int newerThan, BigDecimal progress) {
		logger.debug("Posting status, progress and messages to socket: " + socket.getId());
		// Note that we're not using JobXmlWriter's messagesThreshold argument. It is no use because
		// filtering of messages on log level already happens in MessageBus and JobProgressAppender.
		JobXmlWriter writer = new JobXmlWriter(getJob(), uri);
		writer.withMessages(messages, newerThan);
		writer.withProgress(progress);
		try {
			return postXml(writer.getXmlDocument());
		} catch (UnsupportedOperationException e) {
			// can happen if job is closed
			return false;
		} finally {
			try {
				Status status = getJob().getStatus();
				if (status == Status.SUCCESS || status == Status.ERROR || status == Status.FAIL)
					// this was the last message
					try {
						socket.close();
					} catch (IOException ioe) {
						logger.error(ioe.getMessage());
					}
			} catch (UnsupportedOperationException e) {
				// job is closed
				try {
					socket.close();
				} catch (IOException ioe) {
					logger.error(ioe.getMessage());
				}
			}
		}
	}

	public boolean postStatusUpdate(Status status) {
		logger.debug("Posting status to socket: " + socket.getId());
		JobXmlWriter writer = new JobXmlWriter(getJob(), uri);
		writer.overwriteStatus(status);
		try {
			return postXml(writer.getXmlDocument());
		} finally {
			if (status == Status.SUCCESS || status == Status.ERROR || status == Status.FAIL)
				// this was the last status update
				try {
					socket.close();
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
		}
	}

	private boolean postXml(Document doc) {
		return sendText(XmlUtils.nodeToString(doc));
	}

	private boolean sendText(String message) {
		try {
			socket.getBasicRemote().sendText(message);
			return true;
		} catch (IOException e) {
			logger.error(e.getMessage());
			return false;
		}
	}
}
