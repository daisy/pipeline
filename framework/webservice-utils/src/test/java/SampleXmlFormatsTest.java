import java.io.File;
import java.net.URL;

import net.sf.saxon.dom.DocumentOverNodeInfo;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.pipeline.webserviceutils.xml.XmlValidator;

import org.junit.Assert;
import org.junit.Test;

import org.w3c.dom.Document;

public class SampleXmlFormatsTest {
	
	@Test
	public void validateSampleXmlFormats() throws SaxonApiException {
		assertValid("alive.xml",                 XmlValidator.ALIVE_SCHEMA_URL);
		assertValid("client.xml",                XmlValidator.CLIENT_SCHEMA_URL);
		assertValid("clients.xml",               XmlValidator.CLIENTS_SCHEMA_URL);
		assertValid("error.xml",                 XmlValidator.ERROR_SCHEMA_URL);
		assertValid("job.xml",                   XmlValidator.JOB_SCHEMA_URL);
		assertValid("jobRequest1.localmode.xml", XmlValidator.JOB_REQUEST_SCHEMA_URL);
		assertValid("jobRequest1.xml",           XmlValidator.JOB_REQUEST_SCHEMA_URL);
		assertValid("jobRequest2.xml",           XmlValidator.JOB_REQUEST_SCHEMA_URL);
		assertValid("jobs.xml",                  XmlValidator.JOBS_SCHEMA_URL);
		assertValid("queue.xml",                 XmlValidator.QUEUE_SCHEMA_URL);
		assertValid("script.xml",                XmlValidator.SCRIPT_SCHEMA_URL);
		assertValid("scripts.xml",               XmlValidator.SCRIPTS_SCHEMA_URL);
		assertValid("sizes.xml",                 XmlValidator.SIZES_SCHEMA_URL);
	}
	
	private static void assertValid(String sample, URL schema) throws SaxonApiException {
		Assert.assertTrue(sample + " is not valid",
		                  XmlValidator.validate(
			                  (Document)DocumentOverNodeInfo.wrap(
				                  new Processor(false).newDocumentBuilder().build(
					                  new File(SampleXmlFormatsTest.class.getResource("xml-formats/" + sample).getFile())
				                  ).getUnderlyingNode()),
			                  schema));
	}
}
