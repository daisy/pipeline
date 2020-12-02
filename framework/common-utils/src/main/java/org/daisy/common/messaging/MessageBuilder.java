package org.daisy.common.messaging;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.function.Consumer;
import java.util.List;

import org.daisy.common.messaging.Message.Level;

public final class MessageBuilder {

	private Throwable throwable;
	private String text;
	private Level level;
	private int line;
	private int column;
	private Date timeStamp;
	private String ownerId;
	private String file;
	private BigDecimal portion;
	private List<Consumer<Integer>> callbacks;

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
		throw new UnsupportedOperationException("Sequence number may not be set explicitly.");
	}

	public MessageBuilder withOwnerId(String ownerId) {
		if (this.ownerId != null)
			throw new RuntimeException();
		this.ownerId = ownerId;
		return this;
	}

	public MessageBuilder withFile(String file) {
		this.file = file;
		return this;
	}

	public MessageBuilder withProgress(BigDecimal progress) {
		if (progress.compareTo(BigDecimal.ZERO) < 0 || progress.compareTo(BigDecimal.ONE) > 0)
			throw new IllegalArgumentException("progress must be a number between 0 and 1");
		this.portion = progress;
		return this;
	}

	public MessageBuilder onUpdated(Consumer<Integer> callback) {
		if (callbacks == null)
			callbacks = new ArrayList<Consumer<Integer>>();
		callbacks.add(callback);
		return this;
	}

	MessageImpl build(MessageImpl parent) {
		if (parent != null) {
			if (ownerId == null)
				withOwnerId(parent.ownerId);
			else if (!ownerId.equals(parent.ownerId))
				throw new IllegalArgumentException("Message must belong to same owner as parent");
			if (portion == null)
				portion = BigDecimal.ZERO;
		} else if (portion == null)
			portion = BigDecimal.ONE;
		return new MessageImpl(throwable, text, level, line, column,
				timeStamp, null, ownerId, file, parent, portion,
				callbacks != null ? new ArrayList<Consumer<Integer>>(callbacks) : null);
	}
	
	public MessageAppender build() {
		return build(null);
	}
}
