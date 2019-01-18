import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;

import javax.xml.bind.JAXBElement;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import org.daisy.pipeline.client.PipelineClient;
import org.daisy.pipeline.webservice.jaxb.base.Alive;
import org.daisy.pipeline.webservice.jaxb.job.Job;
import org.daisy.pipeline.webservice.jaxb.job.JobStatus;
import org.daisy.pipeline.webservice.jaxb.job.Result;
import org.daisy.pipeline.webservice.jaxb.request.JobRequest;
import org.daisy.pipeline.webservice.jaxb.request.Priority;
import org.daisy.pipeline.webservice.jaxb.script.Scripts;

import org.junit.Assert;
import org.junit.Test;

public class TestLocalJobs extends Base {
	
	@Test
	public void testAlive() throws Exception {
		logger.info("{} testAlive IN", TestLocalJobs.class);
		Alive alive = client().alive();
		Assert.assertTrue("The version is empty", alive.getVersion().length() > 0);
		Assert.assertTrue("The ws doesn't accept local jobs", alive.getLocalfs().equalsIgnoreCase("true"));
		Assert.assertTrue("The ws needs credentials", alive.getAuthentication().equalsIgnoreCase("false"));
		logger.info("{} testAlive OUT", TestLocalJobs.class);
	}
	
	@Test
	public void testScripts() throws Exception {
		logger.info("{} testScripts IN", TestLocalJobs.class);
		Scripts scripts = client().scripts();
		Assert.assertTrue("There are no scripts in pipeline", scripts.getScript().size() > 0);
		logger.info("{} testScripts OUT", TestLocalJobs.class);
	}
	
	@Test
	public void testSendJob() throws Exception {
		logger.info("{} testSendJob IN", TestLocalJobs.class);
		Optional<JobRequest> req = newJobRequest();
		Assert.assertTrue("Couldn't build the request", req.isPresent());
		Job job = client().sendJob(req.get());
		deleteAfterTest(job);
		Assert.assertTrue("Job has been sent", job.getId() != null && job.getId().length() > 0);
		checkJobInfo(job);
		logger.info("{} testSendJob OUT", TestLocalJobs.class);
	}
	
	private void checkJobInfo(Job in) throws Exception {
		Job job = client().job(in.getId());
		Assert.assertEquals("Ids are not equal", in.getId(), job.getId());
		Assert.assertEquals("Nice name is set", "NICE_NAME",
		                    ((JAXBElement)job.getNicenameOrBatchIdOrScript().get(0)).getValue().toString());
		Assert.assertTrue("Status is set", job.getStatus().value().length() > 0);
		Assert.assertEquals("The priority is low", "low", job.getPriority().toString().toLowerCase());
	}
	
	@Test
	public void testJobStatusCycle() throws Exception {
		logger.info("{} testJobStatusCycle IN", TestLocalJobs.class);
		Optional<JobRequest> req = newJobRequest();
		deleteAfterTest(client().sendJob(req.get()));
		deleteAfterTest(client().sendJob(req.get()));
		Job job = client().sendJob(req.get());
		deleteAfterTest(job);
		Assert.assertEquals("The job status is IDLE", "IDLE", job.getStatus().value());
		job = waitForStatus(JobStatus.RUNNING, job, 10000);
		Assert.assertEquals("The job status is RUNNING", "RUNNING", job.getStatus().value());
		job = waitForStatus(JobStatus.SUCCESS, job, 10000);
		Assert.assertEquals("The job status is SUCCESS", "SUCCESS", job.getStatus().value());
		logger.info("{} testJobStatusCycle OUT", TestLocalJobs.class);
	}
	
	@Test
	public void testAfterJob() throws Exception {
		logger.info("{} testAfterJob IN", TestLocalJobs.class);
		Optional<JobRequest> req = newJobRequest();
		Job job = client().sendJob(req.get());
		job = waitForStatus(JobStatus.SUCCESS, job, 10000);
		checkResults(job);
		checkLog(job);
		checkDelete(job);
		logger.info("{} testAfterJob OUT", TestLocalJobs.class);
	}
	
	@Test
	public void testErrorInJob() throws Exception {
		Optional<JobRequest> req = newJobRequest(client(), Priority.LOW, "mock-error-script", getResource("hello.xml").toURI().toString());
		Assert.assertTrue("The request is present", req.isPresent());
		Job job = client().sendJob(req.get());
		deleteAfterTest(job);
		job = waitForStatus(JobStatus.ERROR, job, 10000);
	}
	
	private void checkDelete(Job in) throws Exception {
		logger.info("{} checking deletion", TestLocalJobs.class);
		PipelineClient client = client();
		client.delete(in.getId());
		try {
			client.job(in.getId());
			Assert.fail("The job shouldn't be here");
		} catch(javax.ws.rs.NotFoundException nfe) {
		}
		File jobData = jobPath(in.getId());
		Assert.assertFalse("Make sure the data folder doesn't exist anymore", Files.isDirectory().apply(jobData));
	}
	
	private void checkLog(Job in) throws IOException {
		logger.info("{} checking log", TestLocalJobs.class);
		String fromServer = client().log(in.getId());
		File logFile = logPath(in.getId());
		String fromFile = Files.toString(logFile,Charset.defaultCharset());
		Assert.assertEquals("The log from the server and the file are equal", fromServer, fromFile);
	}
	
	private void checkResults(Job in) {
		logger.info("{} checking results", TestLocalJobs.class);
		List<Result> results = new JobWrapper(in).getResults().getResult();
		for (Result firstLevelResult : results) {
			for (Result result : firstLevelResult.getResult()) {
				Assert.assertTrue(String.format("The file %s exists",result.getFile()),
				                  Files.isFile().apply(new File(URI.create(result.getFile()))));
			}
		}
	}
}
