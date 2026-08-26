package org.daisy.pipeline.webservice.restlet;

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

import org.daisy.common.properties.Properties;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.job.JobResources;
import org.daisy.pipeline.job.ZippedJobResources;
import org.daisy.pipeline.script.BoundScript;
import org.daisy.pipeline.script.Script;
import org.daisy.pipeline.script.ScriptOption;
import org.daisy.pipeline.script.ScriptPort;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.ScriptService;
import org.daisy.pipeline.webservice.request.JobRequest;
import org.daisy.pipeline.webservice.xml.XmlUtils;

import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Optional;

public class JobSubmitter {

	private static Logger logger = LoggerFactory.getLogger(JobSubmitter.class.getName());

	public static class JobRequestAndData {
		public final JobRequest request;
		public final JobResources data;

		JobRequestAndData(JobRequest request, JobResources data) {
			this.request = request;
			this.data = data;
		}
	}

	public static class BadRequestException extends Exception {
		private static final long serialVersionUID = 1L;

		private BadRequestException(String message) {
			super(message);
		}

		private BadRequestException(Exception cause) {
			super(cause);
		}

		private BadRequestException(String message, Exception cause) {
			super(message, cause);
		}
	}

	private static class LocalInputException extends Exception {
		private static final long serialVersionUID = 1L;

		public LocalInputException(String message) {
			super(message);
		}
	}

	private static final String JOB_DATA_FIELD = "job-data";
	private static final String JOB_REQUEST_FIELD = "job-request";

	public static JobRequestAndData parseJobRequest(AuthenticatedResource resource,
	                                                Representation representation)
			throws BadRequestException {

		Document doc = null;
		ZipFile zipfile = null;
		if (representation == null)
			; // everything will be in the query string
		else if (MediaType.MULTIPART_FORM_DATA.equals(representation.getMediaType(), true)) {
			Request request = resource.getRequest();
			// sort through the multipart request
			MultipartRequestData data = null;
			try {
				data = MultipartRequestData.processMultipart(request,
				                                             JOB_DATA_FIELD,
				                                             JOB_REQUEST_FIELD,
				                                             new File(resource.getConfiguration().getTmpDir()));
			} catch (Exception e) {
				throw new BadRequestException(e);
			}
			if (data == null)
				throw new BadRequestException("Multipart data is empty");
			doc = data.getXml(); // may be null
			zipfile = data.getZipFile();
		} else if (MediaType.APPLICATION_ZIP.equals(representation.getMediaType(), true)) {
			// data is in the ZIP, request is in the query string
			// FIXME: I could not make this work: perhaps it is Restlet, or perhaps it
			// is cURL, but the ZIP file is not identical to the uploaded file
			logger.debug("Reading zip file");
			try {
				File tmp = File.createTempFile("pipeline-ws-", ".zip",
				                               new File(resource.getConfiguration().getTmpDir()));
				try (FileOutputStream fos = new FileOutputStream(tmp)) {
					representation.write(fos);
				}
				zipfile = new ZipFile(tmp);
			} catch (Exception e) {
				throw new BadRequestException(e);
			}
		} else {
			// assuming XML - all data should be inline or on local file system
			String xml = null;
			try {
				xml = representation.getText();
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				InputSource is = new InputSource(new StringReader(xml));
				doc = builder.parse(is);
			} catch (IOException|ParserConfigurationException|SAXException e) {
				if (xml != null && logger.isDebugEnabled())
					logger.debug("Request XML: " + xml);
				throw new BadRequestException(e);
			}
		}
		JobRequest request; {
			if (doc != null)
				try {
					request = JobRequest.fromXML(doc);
				} catch (IllegalArgumentException e) {
					if (doc != null && logger.isDebugEnabled())
						logger.debug("Request XML: " + XmlUtils.nodeToString(doc));
					throw new BadRequestException(e);
				}
			else
				try {
					request = JobRequest.fromQuery(resource.getQuery());
				} catch (IllegalArgumentException e) {
					throw new BadRequestException(e);
				}
		}
		JobResources zip = zipfile != null ? new ZippedJobResources(zipfile) : null;
		return new JobRequestAndData(request, zip);
	}

	public static Job submitJob(AuthenticatedResource resource, JobRequestAndData request, ScriptRegistry scriptRegistry)
			throws BadRequestException {

		JobRequest req = request.request;
		JobResources zip = request.data;
		try {
			ScriptService<?> scriptService = scriptRegistry.getScript(req.getScriptId());
			if (scriptService == null) {
				logger.error("Script not found");
				throw new BadRequestException("Could not create job ");
			}
			Script script = scriptService.load(
				Properties.getProperties(resource.getClient().getId()).getSnapshot());
			BoundScript.Builder bound = new BoundScript.Builder(script, zip);
			addInputsToJob(resource, req.getInputs(), script, bound, zip != null);
			addOptionsToJob(resource, req.getOptions(), script, bound, zip != null);
			if (req.isOutputElementUsed()) {
				// show deprecation warning in server logs
				logger.warn(
					"Deprecated <output/> element used. Job results should be retrieved through the /jobs/ID/result API.");
				// show deprecation warning in response header
				resource.addWarningHeader(
					199,
					"\"Deprecated API\": "
					+ "<output/> is deprecated, job results should be retrieved through the /jobs/ID/result API");
			}
			logger.debug(String.format("Job's nice name: %s", req.getNiceName()));
			logger.debug(String.format("Job's batch ID: %s", req.getBatchId()));
			logger.debug(String.format("Job's priority: %s", req.getPriority()));
			JobManager jobMan = resource.getJobManager(resource.getClient());
			Optional<Job> job = jobMan.newJob(bound.build())
			                          .withNiceName(req.getNiceName())
			                          .withBatchId(req.getBatchId())
			                          .withPriority(req.getPriority())
			                          .build();
			if (!job.isPresent())
				throw new BadRequestException("Could not create job ");
			if (req.getCallbacks().size() > 0) {
				// show deprecation warning in server logs
				logger.warn("Deprecated <callback/> element used. Push notifications should be retrieved through websocket connection.");
				// show deprecation warning in response header
				resource.addWarningHeader(
					199,
					"\"Deprecated API\": "
					+ "<callback/> is deprecated, push notifications should be retrieved through websocket connection");
			}
			return job.get();
		} catch (LocalInputException | FileNotFoundException | IllegalArgumentException e) {
			throw new BadRequestException(e);
		}
	}

	private static void addInputsToJob(AuthenticatedResource resource, Map<String,List<Source>> inputs, Script script,
	                                   BoundScript.Builder builder, boolean zippedContext)
			throws LocalInputException, FileNotFoundException {

		for (ScriptPort input : script.getInputPorts()) {
			String name = input.getName();
			if (inputs.containsKey(name)) {
				for (Source src : inputs.get(name)) {
					InputSource is = SAXSource.sourceToInputSource(src);
					if (is != null && (is.getCharacterStream() != null || is.getByteStream() != null))
						builder.withInput(name, src);
					else {
						URI uri = URI.create(src.getSystemId());
						checkInput(resource, uri, zippedContext);
						builder.withInput(name, uri);
					}
				}
			}
		}
	}

	private static void addOptionsToJob(AuthenticatedResource resource, Map<String,List<String>> options, Script script,
	                                    BoundScript.Builder builder, boolean zippedContext)
			throws LocalInputException, FileNotFoundException {

		for (ScriptOption option : script.getOptions()) {
			String name = option.getName();
			if (options.containsKey(name)) {
				boolean isInput = "anyDirURI".equals(option.getType().getId())
				               || "anyFileURI".equals(option.getType().getId());
				for (String val : options.get(name)) {
					if (isInput)
						checkInput(resource, URI.create(val), zippedContext);
					builder.withOption(name, val);
				}
			}
		}
	}

	private static void checkInput(AuthenticatedResource resource, URI uri, boolean zipFileSupplied) throws LocalInputException {
		if ("file".equals(uri.getScheme())) {
			if (!resource.getConfiguration().isLocalFS()) {
				throw new LocalInputException(
					"WS does not allow local inputs but a href starting with 'file:' was found " + uri);
			} else if (zipFileSupplied) {
				throw new LocalInputException("You can't supply the data uri " + uri);
			}
		}
	}
}
