package org.daisy.common.messaging;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Message {
	/**
	 * Message levels.
	 */
	public enum Level {
		/** The ERROR. */
		ERROR,
		/** The WARNING. */
		WARNING,
		/** The INFO. */
		INFO,
		/** The DEBUG. */
		DEBUG,
		/** The TRACE. */
		TRACE
	}

	protected Throwable throwable;
	protected String text;
	protected Level level;
	protected int line;
	protected int column;
	protected Date timeStamp;
	protected int sequence;
	protected String jobId;
	protected String file;

	protected final static Map<String,Integer> messageCounts = new HashMap<String,Integer>();

	protected Message(Throwable throwable, String text, Level level,
			int line, int column, Date timeStamp, Integer sequence, String jobId,
			String file) {
		this.throwable = throwable;
		this.text = text;
		this.level = level;
		this.line = line;
		this.column = column;
		if (timeStamp == null) {
			timeStamp = new Date();
		}
		this.timeStamp = timeStamp;
		if (sequence == null) {
			if (jobId == null) {
				throw new IllegalStateException("Job ID may not be null");
			}
			sequence = messageCounts.get(jobId);
			if (sequence == null)
				sequence = 0;
			messageCounts.put(jobId, sequence + 1);
		}
		this.sequence = sequence;
		this.jobId = jobId;
		this.file = file;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public String getText() {
		return text;
	}

	public Level getLevel() {
		return level;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}

	public int getSequence() {
		return sequence;
	}

	public String getJobId() {
		return jobId;
	}

	public String getFile() {
		return file;
	}

	public static class MessageBuilder extends AbstractMessageBuilder<Message,MessageBuilder> {
		public Message build() {
			return new Message(throwable, text, level, line, column,
			                   timeStamp, null, jobId, file);
		}
		protected MessageBuilder self() {
			return this;
		}
	}

	public static abstract class AbstractMessageBuilder<M extends Message,B extends AbstractMessageBuilder<M,B>> {

		protected Throwable throwable;
		protected String text;
		protected Level level;
		protected int line;
		protected int column;
		protected Date timeStamp;
		protected String jobId;
		protected String file;
		
		public B withThrowable(Throwable throwable) {
			this.throwable = throwable;
			return self();
		}

		public B withText(String text) {
			this.text = text;
			return self();
		}

		public B withLevel(Level level) {
			this.level = level;
			return self();
		}

		public B withLine(int line) {
			this.line = line;
			return self();
		}

		public B withColumn(int column) {
			this.column = column;
			return self();
		}

		public B withTimeStamp(Date timeStamp) {
			this.timeStamp = timeStamp;
			return self();
		}

		public B withSequence(int sequence) {
			throw new UnsupportedOperationException("Sequence number may not be set explicitly.");
		}

		public B withJobId(String jobId) {
			this.jobId = jobId;
			return self();
		}

		public B withFile(String file) {
			this.file = file;
			return self();
		}

		public B withMessage(Message message) {
			return withThrowable(message.getThrowable())
			      .withText(message.getText())
			      .withLevel(message.getLevel())
			      .withLine(message.getLine())
			      .withColumn(message.getColumn())
			      .withTimeStamp(message.getTimeStamp())
			      .withFile(message.getFile());
		}

		public abstract M build();
		protected abstract B self();

	}
}
