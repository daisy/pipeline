package org.daisy.pipeline.job.impl;

import java.io.IOException;
import java.util.LinkedList;
import java.util.function.Consumer;

import com.google.common.base.Optional;

import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.MessageBus;
import org.daisy.common.properties.Properties;
import org.daisy.common.priority.Priority;
import org.daisy.common.xproc.XProcEngine;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.AbstractJob;
import org.daisy.pipeline.job.AbstractJobContext;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.job.JobMonitorFactory;
import org.daisy.pipeline.job.JobResources;
import org.daisy.pipeline.job.JobResultSet;
import org.daisy.pipeline.job.StatusNotifier;
import org.daisy.pipeline.script.BoundXProcScript;

public class DefaultJobBuilder implements JobManager.JobBuilder {

	private final JobMonitorFactory monitorFactory;
	private final XProcEngine xprocEngine;
	private final Client client;
	private final BoundXProcScript boundScript;
	private final boolean managed;
	private boolean isMapping;
	private JobBatchId batchId;
	private JobResources resources;
	private String niceName = "";
	private Priority priority = Priority.MEDIUM;

	private static Level messagesThreshold;
	static {
		try {
			messagesThreshold = Level.valueOf(
				Properties.getProperty("org.daisy.pipeline.log.level", "INFO"));
		} catch (IllegalArgumentException e) {
			messagesThreshold = Level.INFO;
		}
	}

	/**
	 * @param managed Whether the Job will be managed by a JobManager.
	 */
	public DefaultJobBuilder(JobMonitorFactory monitorFactory,
	                         XProcEngine xprocEngine,
	                         Client client,
	                         BoundXProcScript boundScript,
	                         boolean managed) {
		this.monitorFactory = monitorFactory;
		this.xprocEngine = xprocEngine;
		this.client = client;
		this.boundScript = boundScript;
		this.managed = managed;
	}

	@Override
	public DefaultJobBuilder isMapping(boolean isMapping) {
		this.isMapping = isMapping;
		return this;
	}

	@Override
	public DefaultJobBuilder withResources(JobResources resources) {
		this.resources = resources;
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
				resultMapper = isMapping
					? JobURIUtils.newURIMapper(id.toString())
					: JobURIUtils.newOutputURIMapper(id.toString());
				XProcDecorator decorator = isMapping
					? XProcDecorator.from(script, resultMapper, resources)
					: XProcDecorator.from(script, resultMapper);
				input = decorator.decorate(boundScript.getInput());
				output = decorator.decorate(boundScript.getOutput());
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
			return Optional.of(
				managed ? new AbstractJob(ctxt, priority, xprocEngine, true) {}
				        : new VolatileJob(ctxt, priority, xprocEngine, false));
		} catch (IOException e) {
			throw new RuntimeException("Error while creating job context", e);
		}
	}
}
