package org.daisy.pipeline.webservice.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import javax.websocket.Session;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.ProgressMessage;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.Job.Status;
import org.daisy.pipeline.webservice.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback that posts notification messages on a web socket.
 */
public class WebSocketCallback extends Callback {

	private static Logger logger = LoggerFactory.getLogger(WebSocketCallback.class.getName());

	private final Session socket;

	public WebSocketCallback(Job job, CallbackType type, int frequency, int firstMessage, Session socket) {
		super(job, type, frequency, firstMessage);
		this.socket = socket;
	}

	public boolean postMessages(List<Message> messages, int newerThan, BigDecimal progress) {
		return postMessages(messages, newerThan);
	}

	private boolean postMessages(Iterable<? extends Message> messages, int newerThan) {
		for (Message m : messages) {
			if (m.getSequence() > newerThan && m.getText() != null)
				if (!sendText("[" + m.getLevel() + "] " + m.getText()))
					return false;
			if (m instanceof ProgressMessage)
				if (!postMessages((ProgressMessage)m, newerThan))
					return false;
		}
		return true;
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

	public boolean postStatusUpdate(Status status) {
		return false;
	}
}
