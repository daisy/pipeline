package org.daisy.pipeline.job.impl;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.function.Consumer;

import com.google.common.base.Optional;

import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.MessageBus;
import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.AbstractJob;
import org.daisy.pipeline.job.AbstractJobContext;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.job.JobMonitorFactory;
import org.daisy.pipeline.job.JobResultSet;
import org.daisy.pipeline.job.StatusNotifier;
import org.daisy.pipeline.script.BoundScript;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultJobBuilder implements JobManager.JobBuilder {

	private final JobMonitorFactory monitorFactory;
	private final Client client;
	private final BoundScript boundScript;
	private final boolean managed;
	private final Property logLevelProperty;
	private boolean closeOnExit = false;
	private JobBatchId batchId;
	private String niceName = "";
	private Priority priority = Priority.MEDIUM;

	/**
	 * @param managed Whether the Job will be managed by a JobManager.
	 */
	public DefaultJobBuilder(JobMonitorFactory monitorFactory,
	                         Client client,
	                         BoundScript boundScript,
	                         boolean managed,
	                         Property logLevelProperty) {
		this.monitorFactory = monitorFactory;
		this.client = client;
		this.boundScript = boundScript;
		this.managed = managed;
		this.logLevelProperty = logLevelProperty;
	}

	@Override
	public DefaultJobBuilder closeOnExit() throws UnsupportedOperationException {
		if (managed)
			throw new UnsupportedOperationException();
		this.closeOnExit = closeOnExit;
		return this;
	}

	@Override
	public DefaultJobBuilder withNiceName(String niceName) {
		this.niceName = niceName;
		return this;
	}

	@Override
	public DefaultJobBuilder withPriority(Priority priority) {
		this.priority = priority;
		return this;
	}

	@Override
	public DefaultJobBuilder withBatchId(JobBatchId id) {
		this.batchId = id;
		return this;
	}

	/**
	 * Build the {@link Job} object. May be overridden to add the job to the storage and execute it
	 * upon creation.
	 */
	@Override
	public Optional<Job> build() {
		if (niceName == null ||
		    boundScript == null ||
		    monitorFactory == null)
			throw new IllegalArgumentException("argument must not be null");
		try {
			AbstractJobContext ctxt = new AbstractJobContext() {{
				client = DefaultJobBuilder.this.client;
				batchId = DefaultJobBuilder.this.batchId;
				niceName = DefaultJobBuilder.this.niceName;
				id = JobIdFactory.newId();
				logFile = JobURIUtils.getLogFile(id.toString()).toURI();
				results = JobResultSet.EMPTY;
				script = boundScript.getScript();
				input = boundScript.getInput();
				resultDir = IOHelper.makeDirs(JobURIUtils.getJobOutputDir(id.toString()));
				logger.debug("Storing inputs"); // because we want to be able to download the
					                            // context files of persisted jobs, even though
					                            // JobResources are not persisted and only the
					                            // systemId of ScriptInput are persisted
				File contextDir = IOHelper.makeDirs(JobURIUtils.getJobContextDir(id.toString()));
				input = input.storeToDisk(contextDir);
				properties = Properties.getSnapshot();
				Level messagesThreshold; {
					try {
						messagesThreshold = Level.valueOf(logLevelProperty.getValue(properties));
					} catch (IllegalArgumentException e) {
						messagesThreshold = Level.INFO;
					}
				}
				messageBus = new MessageBus(id.toString(), messagesThreshold);
				statusListeners = new LinkedList<>();
				StatusNotifier statusNotifier = new StatusNotifier() {
						public void listen(Consumer<Job.Status> listener) {
							synchronized (statusListeners) {
								statusListeners.add(listener); }}
						public void unlisten(Consumer<Job.Status> listener) {
							synchronized (statusListeners) {
								statusListeners.remove(listener); }}};
				monitor = monitorFactory.newJobMonitor(id, messageBus, statusNotifier);
			}};
			AbstractJob job = new AbstractJob(ctxt, priority, managed) {};
			if (!managed && closeOnExit)
				job = new VolatileJob(job);
			if (!managed)
				job.changeStatus(Job.Status.IDLE);
			return Optional.of(job);
		} catch (IOException e) {
			throw new RuntimeException("Error while creating job context", e);
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(DefaultJobBuilder.class);

}
