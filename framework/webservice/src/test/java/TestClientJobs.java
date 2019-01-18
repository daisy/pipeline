import java.util.Properties;

import org.daisy.pipeline.client.PipelineClient;
import org.daisy.pipeline.webservice.jaxb.clients.Client;
import org.daisy.pipeline.webservice.jaxb.clients.Priority;
import org.daisy.pipeline.webservice.jaxb.job.Job;
import org.daisy.pipeline.webservice.jaxb.job.JobStatus;
import org.daisy.pipeline.webservice.jaxb.queue.Queue;
import org.daisy.pipeline.webservice.jaxb.request.JobRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;

public class TestClientJobs extends Base {
	
	static Creds CREDS_DEF = new Creds(); static {
		CREDS_DEF.clientId = "clientId";
		CREDS_DEF.secret = "supersecret";
	}
	
	static Creds CREDS_OTHER = new Creds(); static {
		CREDS_OTHER.clientId = "other";
		CREDS_OTHER.secret = "nososecret";
	}
	
	static class Creds {
		String clientId;
		String secret;
	}
	
	@Override
	protected Properties systemProperties() {
		Properties p = super.systemProperties();
		p.setProperty("org.daisy.pipeline.ws.authentication", "true");
		p.setProperty("org.daisy.pipeline.ws.authentication.key", CREDS_DEF.clientId);
		p.setProperty("org.daisy.pipeline.ws.authentication.secret", CREDS_DEF.secret);
		return p;
	}
	
	private static final PipelineClient client = newClient(CREDS_DEF.clientId, CREDS_DEF.secret);
	private static final PipelineClient otherClient = newClient(CREDS_OTHER.clientId, CREDS_OTHER.secret);
	
	@Before
	public void addOtherClient() throws Exception {
		Client other = new Client();
		other.setId(CREDS_OTHER.clientId);
		other.setRole("CLIENTAPP");
		other.setSecret(CREDS_OTHER.secret);
		other.setContact("admin@daisy.org");
		other.setPriority(Priority.LOW);
		deleteAfterTest(client().addClient(other));
	}
	
	@Override
	protected PipelineClient client() {
		return client;
	}
	
	@Test
	public void testJobAccess() throws Exception {
		logger.info("job access IN");
		Optional<JobRequest> req = newJobRequest();
		Job admin = client().sendJob(req.get());
		deleteAfterTest(admin);
		Assert.assertEquals("Admin has one job", 1, client().jobs().getJob().size());
		Assert.assertEquals("Clientapp hasn't got any", 0, otherClient.jobs().getJob().size());
		req = newJobRequest(otherClient);
		Job other = otherClient.sendJob(req.get());
		deleteAfterTest(other);
		Assert.assertEquals("Admin see both jobs", 2, client().jobs().getJob().size());
		Assert.assertEquals("Clientapp only its job", 1, otherClient.jobs().getJob().size());
		try {
			otherClient.job(admin.getId());
			Assert.fail("Clientapp accessed the admin job!");
		} catch (Exception e) {
		}
		try {
			otherClient.delete(admin.getId());
			Assert.fail("Clientapp accessed the admin job!");
		} catch (Exception e){
		}
		Job fromServer = client().job(other.getId());
		Assert.assertNotNull("Admin accessed the client app job", fromServer);
		waitForStatus(JobStatus.SUCCESS, admin, 10000);
		waitForStatus(JobStatus.SUCCESS, other, 10000);
		logger.info("job access OUT");
	}
	
	@Test
	public void testQueueAccess() throws Exception {
		logger.info("queue access IN");
		Optional<JobRequest> req = newJobRequest();
		deleteAfterTest(client().sendJob(req.get()));
		deleteAfterTest(client().sendJob(req.get()));
		deleteAfterTest(client().sendJob(req.get()));
		Job last = client().sendJob(req.get());
		deleteAfterTest(last);
		Queue qAdmin = client().queue();
		Queue qOther = otherClient.queue();
		Assert.assertTrue("Admin queue has jobs", qAdmin.getJob().size() > 0);
		Assert.assertEquals("Clientapp queue hasn't got any", 0, qOther.getJob().size());
		deleteAfterTest(client().sendJob(req.get()));
		deleteAfterTest(client().sendJob(req.get()));
		try {
			otherClient.moveUp(qAdmin.getJob().get(qAdmin.getJob().size() - 1).getId());
			Assert.fail("Other shouldn't be able to move other client jobs around");
		} catch(Exception e){
		}
		waitForStatus(JobStatus.SUCCESS, last, 10000);
		logger.info("queue access OUT");
	}
}
