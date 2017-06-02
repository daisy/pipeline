import com.google.common.base.Optional;

import org.daisy.pipeline.webservice.jaxb.job.Job;
import org.daisy.pipeline.webservice.jaxb.job.JobSizes;
import org.daisy.pipeline.webservice.jaxb.properties.Properties;
import org.daisy.pipeline.webservice.jaxb.request.JobRequest;

import org.junit.Assert;
import org.junit.Test;

public class TestAdmin extends Base {
	
	@Test
	public void testProperties() throws Exception {
		Properties props = client().properties();
		Assert.assertTrue("We have properties", 0 < props.getProperty().size());
	}
	
	@Test
	public void testSizes() throws Exception {
		JobSizes sizes = client().sizes();
		Assert.assertEquals("The current size is 0", sizes.getTotal(), 0);
		Assert.assertEquals("there are no jobs", sizes.getJobSize().size(), 0);
		Optional<JobRequest> req = newJobRequest();
		Assert.assertTrue("Couldn't build the request", req.isPresent());
		Job job = client().sendJob(req.get());
		deleteAfterTest(job);
		waitForStatus("DONE", job, 10000);
		sizes = client().sizes();
		Assert.assertFalse("The current size is not 0", sizes.getTotal() == 0);
		Assert.assertEquals("there is one job", sizes.getJobSize().size(), 1);
	}
}
