import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.ws.rs.core.Response;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;

import org.daisy.pipeline.client.PipelineClient;
import org.daisy.pipeline.webservice.jaxb.base.Alive;
import org.daisy.pipeline.webservice.jaxb.job.Job;
import org.daisy.pipeline.webservice.jaxb.job.Result;
import org.daisy.pipeline.webservice.jaxb.request.JobRequest;
import org.daisy.pipeline.webservice.jaxb.request.Priority;

import org.junit.Assert;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestNonLocalJobs extends Base {
	
	@Override
	protected Properties systemProperties() {
		Properties p = super.systemProperties();
		p.setProperty("org.daisy.pipeline.ws.localfs", "false");
		return p;
	}
	
	@Test
	public void testAlive() throws Exception {
		logger.info("{} testAlive IN", TestNonLocalJobs.class);
		Alive alive = client().alive();
		Assert.assertTrue("The version is empty", alive.getVersion().length() > 0);
		Assert.assertTrue("The ws doesn't accept local jobs", alive.getLocalfs().equalsIgnoreCase("false"));
		logger.info("{} testAlive OUT", TestNonLocalJobs.class);
	}
	
	@Test
	public void testSendJob() throws Exception {
		Optional<JobRequest> req = newJobRequest(Priority.MEDIUM, "hello.xml");
		InputStream is = getResourceAsStream("data.zip");
		Assert.assertTrue("The request is present", req.isPresent());
		Job job = client().sendJob(req.get(), is);
		deleteAfterTest(job);
		Assert.assertTrue("Job has been sent", job.getId() != null && job.getId().length() > 0);
		waitForStatus("DONE", job, 10000);
	}
	
	@Test
	public void testResults() throws Exception {
		Optional<JobRequest> req = newJobRequest(Priority.MEDIUM, "hello.xml");
		InputStream is = getResourceAsStream("data.zip");
		Assert.assertTrue("The request is present", req.isPresent());
		Job job = client().sendJob(req.get(), is);
		deleteAfterTest(job);
		job = waitForStatus("DONE", job, 10000);
		List<Result> results = new JobWrapper(job).getResults().getResult();
		for (Result firstLevelResult : results) {
			checkZippedResult(firstLevelResult);
			for (Result result : firstLevelResult.getResult()){
				checkLeafResult(result);
			}
		}
	}
	
	private void checkZippedResult(Result result) throws IOException {
		logger.info("Getting result {}", result.getHref());
		Response response = client().get(result.getHref().replace(client().getBaseUri(),""));
		InputStream ris = response.readEntity(InputStream.class);
		ZipInputStream zis = new ZipInputStream(ris);
		Assert.assertNotNull("The zip file has entries", zis.getNextEntry());
		ris.close();
	}
	
	private void checkLeafResult(Result result) throws IOException {
		logger.info("Getting result {}", result.getHref());
		Response response = client().get(result.getHref().replace(client().getBaseUri(), ""));
		InputStream ris = response.readEntity(InputStream.class);
		String strRes = CharStreams.toString(new InputStreamReader(ris));
		Assert.assertTrue(String.format("The result has stuff %s", result.getHref()), strRes.length() > 0);
		ris.close();
	}
}
