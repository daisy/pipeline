package org.daisy.common.messaging;

import java.util.Date;

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

	protected Message(Throwable throwable, String text, Level level,
			int line, int column, Date timeStamp, int sequence, String jobId,
			String file) {
		this.throwable = throwable;
		this.text = text;
		this.level = level;
		this.line = line;
		this.column = column;
		this.timeStamp = timeStamp;
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

	public static class MessageBuilder {
		Throwable throwable;
		String text;
		Level level;
		int line;
		int column;
		Date timeStamp;
		int sequence;
		String jobId;
		String file;


		public MessageBuilder withThrowable(Throwable throwable) {
			this.throwable = throwable;
			return this;
		}


		public MessageBuilder withText(String text) {
			this.text = text;
			return this;
		}


		public MessageBuilder withLevel(Level level) {
			this.level = level;
			return this;
		}


		public MessageBuilder withLine(int line) {
			this.line = line;
			return this;
		}


		public MessageBuilder withColumn(int column) {
			this.column = column;
			return this;
		}


		public MessageBuilder withTimeStamp(Date timeStamp) {
			this.timeStamp = timeStamp;
			return this;
		}


		public MessageBuilder withSequence(int sequence) {
			this.sequence = sequence;
			return this;
		}


		public MessageBuilder withJobId(String jobId) {
			this.jobId = jobId;
			return this;
		}


		public MessageBuilder withFile(String file) {
			this.file = file;
			return this;
		}

		public Message build() {
			return new Message(throwable, text, level, line, column,
					timeStamp, sequence, jobId, file);
		}

	}


}
