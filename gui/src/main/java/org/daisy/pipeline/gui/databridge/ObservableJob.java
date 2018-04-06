package org.daisy.pipeline.gui.databridge;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.Message.Level;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.Job.Status;

// translate the Pipeline2 Job object into GUI-friendly Strings and StringProperty objects
public class ObservableJob implements Comparable<ObservableJob> { //extends SimpleObjectProperty {

	private final int sequenceNumber;
	private final StringProperty status;
	private final ObservableList<String> messages;
	private final Job job;
	private final BoundScript boundScript; // store the job parameters here for display later
	private final StringProperty toString;
	
	public ObservableJob(Job job, BoundScript boundScript, int sequenceNumber) {
		this.job = job;
		this.boundScript = boundScript;
		this.sequenceNumber = sequenceNumber;
		status = new SimpleStringProperty();
		toString = new SimpleStringProperty();
		setStatus(job.getStatus());
		messages = FXCollections.observableArrayList();
		addInitialMessages();
	}
	
	public String getStatus() {
		return status.get();
	}
	public void setStatus(Status status) {
		Platform.runLater(() -> {
				this.status.set(statusToString(status));
				updateToString();
			}
		);
	}
	public StringProperty statusProperty() {
		return this.status;
	}
	public ObservableList<String> getMessages() {
		return this.messages;
	}
	public void addMessage(String message, Level level) {
		Platform.runLater(() -> {
				this.messages.add(formatMessage(message, level));
			});
	}
	public Job getJob() {
		return job;
	}
	public BoundScript getBoundScript() {
		return boundScript;
	}
	private void addInitialMessages() {
		Iterable<Message> messages = job.getContext().getMonitor().getMessageAccessor().getAll();
		for (Message message : messages) {
			addMessage(message.getText(), message.getLevel());
		}
	}
	private static String statusToString(Status status) {
		if (status == Status.DONE) {
			return "Done";
		}
		if (status == Status.ERROR) {
			return "Error";
		}
		if (status == Status.IDLE) {
			return "Idle";
		}
		if (status == Status.RUNNING) {
			return "Running";
		}
		if (status == Status.VALIDATION_FAIL) {
			return "Validation fail";
		}
		return "";
	}
	private static String formatMessage(String message, Level level) {
		return level.toString() + ": " + message;
	}
	
	@Override
	public String toString() {
		return "#" + sequenceNumber + " - " + boundScript.getScript().getName() + " - " + status.get();
	}
	
	public StringProperty toStringProperty() {
		return toString;
	}
	
	private void updateToString() {
		toString.set(toString());
	}
	
	@Override
	public int compareTo(ObservableJob o) {
		if (o.sequenceNumber == sequenceNumber)
			return 0;
		else if (o.sequenceNumber < sequenceNumber)
			return 1;
		else
			return -1;
	}
}
