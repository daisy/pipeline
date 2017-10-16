import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

import org.daisy.pipeline.client.filestorage.JobStorage;
import org.daisy.pipeline.client.http.WS;
import org.daisy.pipeline.client.http.WSInterface;
import org.daisy.pipeline.client.models.Argument;
import org.daisy.pipeline.client.models.Job;
import org.daisy.pipeline.client.models.Script;
import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.utils.XML;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class WSRemoteInDockerTest extends AbstractTest {
	
	static final File TARGET_DIR = new File(WSRemoteInDockerTest.class.getResource("/").getPath()).getParentFile();
	
	static final String WS_HOST = System.getenv("PIPELINE2_WS_HOST");
	static final String WS_PORT = System.getenv("PIPELINE2_WS_PORT");
	static final String WS_PATH = "ws";
	
	String getEndpoint() {
		return "http://" + WS_HOST + ":" + WS_PORT + "/" + WS_PATH;
	}
	
	@Test
	public void testJobRemote() throws InterruptedException, ZipException, IOException {
		WSInterface ws = new WS();
		ws.setEndpoint(getEndpoint());
		assertFalse(ws.alive().localfs);
		Job job; {
			job = new Job();
			job.setId("1");
			Script script = ws.getScript("dtbook-to-pef");
			job.setScript(script);
			File jobStorageDir = new File(TARGET_DIR, "tmp/client/jobs");
			JobStorage context = new JobStorage(job, jobStorageDir, null);
			Argument source = script.getArgument("source");
			source.set(new File(TARGET_DIR, "test-classes/DTB23151.xml"), context);
		}
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		             "<jobRequest xmlns=\"http://www.daisy.org/ns/pipeline/data\">\n" +
		             "   <script href=\"" + getEndpoint() + "/scripts/dtbook-to-pef\"/>\n" +
		             "   <input name=\"source\">\n" +
		             "      <item value=\"DTB23151.xml\"/>\n" +
		             "   </input>\n" +
		             "</jobRequest>\n",
		             XML.toString(job.toJobRequestXml(false)));
		assertNull(job.validate());
		job.getJobStorage().save(false);
		ZipFile contextZip = new ZipFile(job.getJobStorage().makeContextZip());
		assertNotNull(contextZip.getEntry("DTB23151.xml"));
		contextZip.close();
		job = ws.postJob(job);
		assertNotNull(job);
		
		// FIXME: wait for status DONE
		Thread.sleep(30000);
		job = ws.getJob(job.getId(), 0);
		
		// FIXME: show error messages if status = ERROR?
		assertEquals("The job is finished", Job.Status.DONE, job.getStatus());
		StringWriter writer = new StringWriter();
		assertEquals(getEndpoint() + "/jobs/" + job.getId(), job.getHref());
		String resultHref = job.getResults().get(job.getResult("pef-output-dir")).get(0).href;
		assertEquals(job.getHref() + "/result/option/pef-output-dir/idx/pef-output-dir/DTB23151.pef", resultHref);
		IOUtils.copy(new URL(resultHref).openStream(), writer);
	}
}
