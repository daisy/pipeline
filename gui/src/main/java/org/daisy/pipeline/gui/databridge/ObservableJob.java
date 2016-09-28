package org.daisy.pipeline.gui.databridge;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.Message.Level;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.Job.Status;

// translate the Pipeline2 Job object into GUI-friendly Strings and StringProperty objects
public class ObservableJob { //extends SimpleObjectProperty {

	private StringProperty status;
	private ObservableList<String> messages;
	private Job job;
	BoundScript boundScript; // store the job parameters here for display later
	
	public ObservableJob(Job job) {
		status = new SimpleStringProperty();
		this.job = job;
		this.setStatus(job.getStatus());
		messages = FXCollections.observableArrayList();
		addInitialMessages();
	}
	
	public String getStatus() {
		return status.get();
	}
	public void setStatus(Status status) {
		this.status.set(statusToString(status));
	}
	public StringProperty statusProperty() {
		return this.status;
	}
	public ObservableList<String> getMessages() {
		return this.messages;
	}
	public void addMessage(String message, Level level) {
		this.messages.add(formatMessage(message, level));
	}
	public Job getJob() {
		return job;
	}
	public void setBoundScript(BoundScript boundScript) {
		this.boundScript = boundScript;
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
	
}
