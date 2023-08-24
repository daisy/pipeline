import com.google.common.base.Optional;

import org.daisy.pipeline.webservice.jaxb.job.Job;
import org.daisy.pipeline.webservice.jaxb.job.JobSizes;
import org.daisy.pipeline.webservice.jaxb.job.JobStatus;
import org.daisy.pipeline.webservice.jaxb.properties.Properties;
import org.daisy.pipeline.webservice.jaxb.properties.Property;
import org.daisy.pipeline.webservice.jaxb.request.JobRequest;

import org.junit.Assert;
import org.junit.Test;

public class TestAdmin extends Base {
	
	@Test
	public void testSizes() throws Exception {
		JobSizes sizes = client().sizes();
		Assert.assertEquals("The current size is 0", sizes.getTotal(), 0);
		Assert.assertEquals("there are no jobs", sizes.getJobSize().size(), 0);
		Optional<JobRequest> req = newJobRequest();
		Assert.assertTrue("Couldn't build the request", req.isPresent());
		Job job = client().sendJob(req.get());
		deleteAfterTest(job);
		waitForStatus(JobStatus.SUCCESS, job, 10000);
		sizes = client().sizes();
		Assert.assertFalse("The current size is not 0", sizes.getTotal() == 0);
		Assert.assertEquals("there is one job", sizes.getJobSize().size(), 1);
	}

	private final static String LOG_LEVEL = "org.daisy.pipeline.log.level";

	@Override
	protected java.util.Properties systemProperties() {
		java.util.Properties p = super.systemProperties();
		p.setProperty(LOG_LEVEL, "INFO");
		return p;
	}

	@Test
	public void testProperties() throws Exception {
		Properties props = client().properties();
		Assert.assertTrue("We have properties", props.getProperty().size() > 0);
		for (Property p : props.getProperty()) {
			if (LOG_LEVEL.equals(p.getName()))
				return;
		}
		Assert.fail("No property named '" + LOG_LEVEL + "'");
	}

	@Test
	public void testProperty() throws Exception {
		Property prop = client().property(LOG_LEVEL);
		Assert.assertNotNull("No property named '" + LOG_LEVEL + "'", prop);
		Assert.assertEquals("INFO", prop.getValue());
		Property modified = new Property();
		modified.setName(LOG_LEVEL);
		modified.setValue("DEBUG");
		Property fromServer = client().updateProperty(modified);
		Assert.assertEquals("Property name", fromServer.getName(), modified.getName());
		Assert.assertEquals("Property value", fromServer.getValue(), modified.getValue());
	}
}
