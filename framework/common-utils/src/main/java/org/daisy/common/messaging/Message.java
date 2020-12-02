package org.daisy.common.messaging;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple message
 */
public class Message {

	/**
	 * Severity levels.
	 */
	public enum Level {
		ERROR,
		WARNING,
		INFO,
		DEBUG,
		TRACE;

		public boolean isMoreSevereThan(Level other) {
			return compareTo(other) < 0;
		}
	}

	protected Throwable throwable;
	protected String text;
	protected Level level;
	protected int line;
	protected int column;
	protected Date timeStamp;
	protected int sequence;
	protected String ownerId;
	protected String file;

	protected final static Map<String,Integer> messageCounts = new HashMap<String,Integer>();

	protected Message(Throwable throwable, String text, Level level,
			int line, int column, Date timeStamp, Integer sequence, String ownerId,
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
			if (ownerId == null) {
				throw new IllegalStateException("Owner ID may not be null");
			}
			sequence = messageCounts.get(ownerId);
			if (sequence == null)
				sequence = 0;
			messageCounts.put(ownerId, sequence + 1);
		}
		this.sequence = sequence;
		this.ownerId = ownerId;
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

	/**
	 * A string that uniquely identifies the "owner" of this message, for example a pipeline job.
	 */
	public String getOwnerId() {
		return ownerId;
	}

	public String getFile() {
		return file;
	}
}
