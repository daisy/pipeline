package org.daisy.pipeline.webservice;

import java.math.BigDecimal;
import java.util.List;

import org.daisy.common.messaging.Message;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.Job.Status;

public abstract class Callback {

	public enum CallbackType {STATUS, PROGRESS, MESSAGES}

	private final CallbackType type;
	private final Job job;
	private final int frequency;
	private final int firstMessage;

	/**
	 * @param firstMessage sequence number of the first expected message (0-based).
	 */
	public Callback(Job job, CallbackType type, int frequency, int firstMessage) {
		this.type = type;
		this.job = job;
		this.frequency = frequency;
		this.firstMessage = firstMessage;
	}

	public Job getJob() {
		return job;
	}

	public CallbackType getType() {
		return type;
	}

	public int getFrequency() {
		return frequency;
	}

	/**
	 * Sequence number of the first expected message.
	 */
	public int getFirstMessage() {
		return firstMessage;
	}

	public abstract boolean postMessages(List<Message> messages, int newerThan, BigDecimal progress);

	public abstract boolean postProgress(BigDecimal progress);

	public abstract boolean postStatusUpdate(Status status);

}
