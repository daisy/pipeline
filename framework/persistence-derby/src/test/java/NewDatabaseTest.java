import java.util.Iterator;
import javax.inject.Inject;

import com.google.common.base.Optional;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.ClientStorage;
import org.daisy.pipeline.clients.WebserviceStorage;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.job.JobManagerFactory;
import org.daisy.pipeline.job.JobStorage;
import org.daisy.pipeline.script.BoundScript;
import org.daisy.pipeline.script.XProcScriptService;

import org.junit.Assert;
import org.junit.Test;

public class NewDatabaseTest extends TestBase {
	
	@Inject
	public JobStorage jobStorage;
	
	@Inject
	public WebserviceStorage webserviceStorage;
	
	@Inject
	public XProcScriptService script;
	
	@Test
	public void testClientStorage() {
		ClientStorage clientStorage = webserviceStorage.getClientStorage();
		Optional<Client> client = clientStorage.addClient("my-client", "my-secret", Client.Role.CLIENTAPP, "me@daisy.org");
		Assert.assertTrue(client.isPresent());
		Assert.assertEquals("my-client", client.get().getId());
		Iterator<? extends Client> allClients = clientStorage.getAll().iterator();
		Assert.assertTrue(allClients.hasNext());
		Assert.assertEquals(client.get(), allClients.next());
		Assert.assertFalse(allClients.hasNext());
		Assert.assertTrue(clientStorage.delete("my-client"));
		Assert.assertTrue(clientStorage.getAll().isEmpty());
	}
	
	@Test
	public void testJobStorage() {
		ClientStorage clientStorage = webserviceStorage.getClientStorage();
		Optional<Client> client = clientStorage.addClient("my-client", "my-secret", Client.Role.CLIENTAPP, "me@daisy.org");
		Assert.assertTrue(client.isPresent());
		Assert.assertEquals("my-script", script.getId());
		BoundScript boundScript = new BoundScript.Builder(script.load()).build();
		JobManager jobManager; {
			JobManagerFactory factory = new JobManagerFactory() {{
				setJobStorage(jobStorage);
				init(); }};
			jobManager = factory.createFor(client.get());
		}
		Optional<Job> job = jobManager.newJob(boundScript)
		                              .withNiceName("my-job")
		                              .build();
		Assert.assertTrue(job.isPresent());
		jobStorage.remove(job.get().getId());
		Assert.assertFalse(jobStorage.iterator().hasNext());
		clientStorage.delete("my-client");
		Assert.assertTrue(clientStorage.getAll().isEmpty());
	}
}
