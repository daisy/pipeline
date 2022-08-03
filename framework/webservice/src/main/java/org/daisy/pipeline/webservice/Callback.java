package org.daisy.pipeline.webservice;

import java.math.BigDecimal;
import java.util.List;

import org.daisy.common.messaging.Message;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.Job.Status;

public abstract class Callback {

	public enum CallbackType {STATUS, MESSAGES}

	private final CallbackType type;
	private final Job job;
	private int frequency = 1;

	public Callback(Job job, CallbackType type, int frequency) {
		this.type = type;
		this.job = job;
		this.frequency = frequency;
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

	public abstract boolean postMessages(List<Message> messages, int newerThan, BigDecimal progress);

	public abstract boolean postStatusUpdate(Status status);

}
