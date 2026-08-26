package org.daisy.pipeline.ocr.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.daisy.common.messaging.Message.Level;
import org.daisy.common.priority.Priority;
import org.daisy.common.properties.Properties;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.job.JobResources;
import org.daisy.pipeline.job.ZippedJobResources;
import org.daisy.pipeline.script.BoundScript;
import org.daisy.pipeline.script.Script;
import org.daisy.pipeline.script.ScriptOption;
import org.daisy.pipeline.script.ScriptPort;
import org.daisy.pipeline.script.ScriptService;
import org.daisy.pipeline.webservice.CallbackHandler;
import org.daisy.pipeline.webservice.impl.PosterCallback;
import org.daisy.pipeline.webservice.request.JobRequest;
import org.daisy.pipeline.webservice.restlet.AuthenticatedResource;
import org.daisy.pipeline.webservice.restlet.JobSubmitter;
import org.daisy.pipeline.webservice.restlet.JobSubmitter.BadRequestException;
import org.daisy.pipeline.webservice.restlet.JobSubmitter.JobRequestAndData;
import org.daisy.pipeline.webservice.xml.JobXmlWriter;
import org.daisy.pipeline.webservice.xml.XmlUtils;

import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Optional;

public class OCRJobsResource extends AuthenticatedResource {

	private static Logger logger = LoggerFactory.getLogger(OCRJobsResource.class.getName());

	@Post
	public Representation createResource(Representation representation) {
		logRequest();
		maybeEnableCORS();
		if (!isAuthenticated()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}
		OCRScriptsWebServiceExtension registry
			= (OCRScriptsWebServiceExtension)getContext().getAttributes().get(OCRScriptsResource.OCR_SCRIPTS_KEY);
		try {
			JobRequestAndData req = JobSubmitter.parseJobRequest(this, representation);
			Job job = JobSubmitter.submitJob(this, req, registry.getScriptRegistry(getClient()));

			// store the config
			Document doc = req.request.toXML();
			getStorage().getJobConfigurationStorage().add(
				job.getId(),
				doc != null ? XmlUtils.nodeToString(doc) : req.request.toJSON());

			// Note that we're not using JobXmlWriter's messagesThreshold argument. It is no use because
			// filtering of messages on log level already happens in MessageBus and JobProgressAppender.
			// Also note we are not passing "/ocr/jobs/{id}" as the route, since "/ocr/jobs" is only used
			// for posting OCR jobs. For now the existing /jobs endpoint is used to retrieve the jobs.
			// (This is probably not RESTful.)
			JobXmlWriter writer = new JobXmlWriter(job,
			                                       getRequest().getRootRef().toString(),
			                                       getWebSocketRootRef().toString());
			Priority jobPriority
				= getJobManager(getStorage().getClientStorage().defaultClient()).getExecutionQueue()
				                                                                .getJobPriority(job.getId());
			if (job.getStatus() == Job.Status.IDLE)
				writer.withPriority(jobPriority);
			Document jobXml = writer.withScriptDetails()
			                        .withNotificationsAttribute()
			                        .getXmlDocument();
			DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, jobXml);
			setStatus(Status.SUCCESS_CREATED);
			logResponse(dom);
			return dom;
		} catch (BadRequestException e) {
			logger.error("Bad request:", e);
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return getErrorRepresentation(e.getMessage());
		}
	}
}
