package org.daisy.pipeline.webservice.impl;

import java.io.File;
import java.net.URI;

import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobManager;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

// TODO: Auto-generated Javadoc
/**
 * The Class LogResource.
 */

public class LogResource extends AuthenticatedResource {
	/** The job. */
	private Optional<Job> job=Optional.absent();
	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(LogResource.class.getName());

	/* (non-Javadoc)
	 * @see org.restlet.resource.Resource#doInit()
	 */
	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthenticated()) {
			return;
		}

		JobManager jobMan = webservice().getJobManager(this.getClient());
		String idParam = (String) getRequestAttributes().get("id");

		try {
			JobId id = JobIdFactory.newIdFromString(idParam);
			job = jobMan.getJob(id);
		}
		catch(Exception e) {
			logger.error(e.getMessage());
		}

	}

	/*
	 * example output: daisy-pipeline/webservice/docs/sampleXml/log.xml
	 */
	/**
	 * Gets the resource.
	 *
	 * @return the resource
	 */
	@Get
	public Representation getResource() {
		if (!isAuthenticated()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}

		if (!job.isPresent()) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return this.getErrorRepresentation("Job not found");
		}

		setStatus(Status.SUCCESS_OK);

		FileRepresentation logfile;
		URI logfileUri = job.get().getContext().getLogFile();
		if (logfileUri != null) {
			logfile = new FileRepresentation(new File(logfileUri), MediaType.TEXT_PLAIN);
			return logfile;
		} else {
			return this.getErrorRepresentation("Log file was not present");
		}
	}
}
