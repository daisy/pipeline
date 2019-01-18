package org.daisy.pipeline.event;

/**
 * Event for notifying that a job message that has been previously put on the event bus
 * has been updated.
 */
public class ProgressMessageUpdate {

	private final ProgressMessage message;
	private final int sequence;

	ProgressMessageUpdate(ProgressMessage message, int sequence) {
		this.message = message;
		this.sequence = sequence;
	}

	/** The message that has been updated */
	public ProgressMessage getMessage() {
		return message;
	}

	/** Sequence number that represents the update */
	public int getSequence() {
		return sequence;
	}
	
	@Override
	public String toString() {
		return sequence + " within " + message;
	}
}
