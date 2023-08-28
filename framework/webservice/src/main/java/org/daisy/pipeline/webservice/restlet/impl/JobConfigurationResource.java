package org.daisy.pipeline.webservice.restlet.impl;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.webservice.restlet.AuthenticatedResource;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Optional;

public class JobConfigurationResource extends AuthenticatedResource {
	/** The job. */
	private Optional<Job> job=Optional.absent();
	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(JobConfigurationResource.class.getName());

	/* (non-Javadoc)
	 * @see org.restlet.resource.Resource#doInit()
	 */
	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthenticated()) {
			return;
		}

		JobManager jobMan = getJobManager(this.getClient());
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
		logRequest();
		maybeEnableCORS();
		if (!isAuthenticated()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}

		if (!job.isPresent()) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return this.getErrorRepresentation("Job not found");
		}
		
		String xml = getStorage().getJobConfigurationStorage().get(job.get().getId());
		Document doc;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xml));
			doc = builder.parse(is);
		} catch (IOException e) {
			logger.error(e.getMessage());
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return this.getErrorRepresentation(e);
		} catch (ParserConfigurationException e) {
			logger.error(e.getMessage());
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return this.getErrorRepresentation(e);
		} catch (SAXException e) {
			logger.error(e.getMessage());
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return this.getErrorRepresentation(e);
		}

		DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, doc);
		setStatus(Status.SUCCESS_OK);
		logResponse(dom);
		return dom;
	}
}