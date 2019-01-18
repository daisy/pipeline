package org.daisy.pipeline.event;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;

import org.daisy.common.messaging.Message.AbstractMessageBuilder;

public class ProgressMessageBuilder extends AbstractMessageBuilder<ProgressMessage,ProgressMessageBuilder> {

	private BigDecimal portion;
	private List<Consumer<ProgressMessageUpdate>> callbacks;

	public ProgressMessageBuilder withProgress(BigDecimal progress) {
		if (progress.compareTo(BigDecimal.ZERO) < 0 || progress.compareTo(BigDecimal.ONE) > 0)
			throw new IllegalArgumentException("progress must be a number between 0 and 1");
		this.portion = progress;
		return this;
	}

	ProgressMessageBuilder onUpdated(Consumer<ProgressMessageUpdate> callback) {
		if (callbacks == null)
			callbacks = new ArrayList<Consumer<ProgressMessageUpdate>>();
		callbacks.add(callback);
		return this;
	}

	ProgressMessage build(ProgressMessageImpl parent) {
		if (parent != null) {
			if (jobId == null)
				withJobId(parent.jobId);
			else if (!jobId.equals(parent.jobId))
				throw new IllegalArgumentException("Message must belong to same job as parent");
			if (portion == null)
				portion = BigDecimal.ZERO;
		} else if (portion == null)
			portion = BigDecimal.ONE;
		return new ProgressMessageImpl(throwable, text, level, line, column,
				timeStamp, null, jobId, file, parent, portion,
				callbacks != null ? new ArrayList<Consumer<ProgressMessageUpdate>>(callbacks) : null);
	}
	
	public ProgressMessage build() {
		return build(null);
	}

	public ProgressMessageBuilder self() {
		return this;
	}
}
