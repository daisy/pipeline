package org.daisy.pipeline.client.http;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

import org.daisy.pipeline.client.filestorage.JobStorage;
import org.daisy.pipeline.client.models.Argument;
import org.daisy.pipeline.client.models.Job;
import org.daisy.pipeline.client.models.Script;
import org.daisy.pipeline.client.utils.XML;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class WSRemoteTest extends PaxExamConfig {
	
	@Override
	boolean isLocalFs() {
		return false;
	}
	
	@Test
	public void testJobRemote() throws InterruptedException, ZipException, IOException {
		WSInterface ws = new WS();
		assertFalse(ws.alive().localfs);
		Job job; {
			job = new Job();
			job.setId("1");
			Script script = ws.getScript("foo:script");
			job.setScript(script);
			File jobStorageDir = new File(BASEDIR, "target/tmp/client/jobs");
			JobStorage context = new JobStorage(job, jobStorageDir, null);
			Argument source = script.getArgument("source");
			source.set(new File(BASEDIR, "src/test/resources/input1.xml"), context);
			Argument option1 = script.getArgument("option-1");
			option1.set("three");
			Argument href = script.getArgument("href");
			href.set(new File(BASEDIR, "src/test/resources/input2.html"), context);
		}
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		             "<jobRequest xmlns=\"http://www.daisy.org/ns/pipeline/data\">\n" +
		             "   <script href=\"http://localhost:8181/ws/scripts/foo:script\"/>\n" +
		             "   <input name=\"source\">\n" +
		             "      <item value=\"input1.xml\"/>\n" +
		             "   </input>\n" +
		             "   <option name=\"option-1\">three</option>\n" +
		             "   <option name=\"href\">input2.html</option>\n" +
		             "</jobRequest>\n",
		             XML.toString(job.toJobRequestXml(false)));
		assertNull(job.validate());
		job.getJobStorage().save(false);
		ZipFile contextZip = new ZipFile(job.getJobStorage().makeContextZip());
		assertNotNull(contextZip.getEntry("input1.xml"));
		assertNotNull(contextZip.getEntry("input2.html"));
		contextZip.close();
		job = ws.postJob(job);
		assertNotNull(job);
		Thread.sleep(2000);
		job = ws.getJob(job.getId(), 0);
		assertEquals("The job is finished", Job.Status.SUCCESS, job.getStatus());
		String result; {
			StringWriter writer = new StringWriter();
			IOUtils.copy(new URL(job.getResults().get(job.getResult("output-dir")).get(0).href).openStream(),
			             writer);
			result = writer.toString();
		}
		assertEquals("<result>" +
		             "<source><hello object=\"world\"/></source>" +
		             "<option name=\"href\" value=\""
		                 + new File(PIPELINE_DATA, "jobs/" + job.getId() + "/context/input2.html").toURI() + "\"/>" +
		             "</result>",
		             result);
	}
}
