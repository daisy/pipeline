import java.util.Properties;

import javax.ws.rs.NotAuthorizedException;

import org.daisy.pipeline.client.PipelineClient;

import org.junit.Assert;
import org.junit.Test;

public class TestAuthenticationErrors extends Base {
	
	private static final PipelineClient client = newClient(TestClientJobs.CREDS_DEF.clientId, TestClientJobs.CREDS_DEF.secret);
	
	@Override
	protected PipelineClient client() {
		return client;
	}
	
	@Override
	protected Properties systemProperties() {
		Properties p = super.systemProperties();
		p.setProperty("org.daisy.pipeline.ws.authentication", "true");
		p.setProperty("org.daisy.pipeline.ws.authentication.key", TestClientJobs.CREDS_DEF.clientId);
		p.setProperty("org.daisy.pipeline.ws.authentication.secret", TestClientJobs.CREDS_DEF.secret);
		return p;
	}
	
	@Test
	public void testAccessWithoutPermissions() throws Exception {
		PipelineClient bogus = newClient("notallowed", "whatevs");
		try {
			bogus.scripts();
			Assert.fail();
		} catch (NotAuthorizedException e) {
		}
	}
}
