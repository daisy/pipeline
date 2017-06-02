import java.util.List;

import com.google.common.base.Optional;

import org.daisy.pipeline.webservice.jaxb.request.Priority;
import org.daisy.pipeline.webservice.jaxb.job.Job;
import org.daisy.pipeline.webservice.jaxb.job.Jobs;
import org.daisy.pipeline.webservice.jaxb.request.JobRequest;

import org.junit.Assert;
import org.junit.Test;

public class TestMultipleJobs extends Base {

	@Test
	public void testMultipleJobs() throws Exception {
		logger.info("{} testMultipleJobs IN", TestLocalJobs.class);
		Optional<JobRequest> req = newJobRequest();
		Job job1 = client().sendJob(req.get());
		Job job2 = client().sendJob(req.get());
		Job job3 = client().sendJob(req.get());
		Jobs jobs = client().jobs();
		Assert.assertEquals("we have 3 jobs", 3, jobs.getJob().size());
		waitForStatus("DONE", job1, 10000);
		client().delete(job1.getId());
		client().delete(job2.getId());
		client().delete(job3.getId());
		logger.info("{} testMultipleJobs OUT", TestLocalJobs.class);
	}
	
	@Test
	public void testPriorites() throws Exception {
		logger.info("{} testQueue IN", TestLocalJobs.class);
		Priority[] prios = new Priority[]{Priority.HIGH,
		                                  Priority.HIGH,
		                                  Priority.HIGH,
		                                  Priority.LOW,
		                                  Priority.MEDIUM,
		                                  Priority.HIGH};
		Job lastJob = null;
		for (int i = 0; i < 6; i++) {
			Optional<JobRequest> req = newJobRequest(prios[i]);
			lastJob = client().sendJob(req.get());
			deleteAfterTest(lastJob);
			if (i == 2) { // wait to have a more equal relative time for the next 3 jobs
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}
		}
		Assert.assertEquals("We have 6 jobs", 6, client().jobs().getJob().size());
		List<org.daisy.pipeline.webservice.jaxb.queue.Job> queue = client().queue().getJob();
		Assert.assertTrue("There are at least 4 jobs in the queue", queue.size() >= 4);
		org.daisy.pipeline.webservice.jaxb.queue.Job last = queue.get(queue.size() - 1);
		// As the algorithm is time dependent it has different behaviours depending
		// on the machine this test is exectuted
		// Assert.assertEquals("last job has priority low", "low", last.getJobPriority().value());
		// Assert.assertEquals("next to last job has priority medium", "medium", queue.get(queue.size() - 2).getJobPriority().value());
		// Assert.assertEquals("first job has priority high", "high", queue.get(queue.size() - 3).getJobPriority().value());
		queue = client().moveUp(last.getId()).getJob();
		Assert.assertEquals("The last job has been moved up", last.getId(), queue.get(queue.size() - 2).getId());
		queue = client().moveDown(last.getId()).getJob();
		Assert.assertEquals("The last job has been moved down", last.getId(), queue.get(queue.size() - 1).getId());
		waitForStatus("DONE", lastJob, 10000);
		logger.info("{} testQueue OUT", TestLocalJobs.class);
	}
}
