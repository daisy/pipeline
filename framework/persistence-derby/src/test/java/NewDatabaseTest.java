import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import javax.inject.Inject;
import javax.xml.transform.stream.StreamResult;

import com.google.common.base.Optional;

import org.daisy.common.priority.Priority;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.ClientStorage;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobContext;
import org.daisy.pipeline.job.JobContextFactory;
import org.daisy.pipeline.job.JobStorage;
import org.daisy.pipeline.script.BoundXProcScript;
import org.daisy.pipeline.script.XProcScriptService;
import org.daisy.pipeline.webserviceutils.storage.WebserviceStorage;

import org.junit.Assert;
import org.junit.Test;

public class NewDatabaseTest extends OSGiTestBase {
	
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
		JobContextFactory jobContextFactory = new JobContextFactory(client.get());
		Assert.assertEquals("my-script", script.getId());
		BoundXProcScript boundScript = BoundXProcScript.from(
			script.load(),
			new XProcInput.Builder().build(),
			new XProcOutput.Builder()
			    .withOutput("result", () -> new StreamResult(new ByteArrayOutputStream()))
			    .build());
		JobContext jobContext = jobContextFactory.newJobContext("my-job", null, boundScript);
		Optional<Job> job = jobStorage.add(Priority.MEDIUM, jobContext);
		Assert.assertTrue(job.isPresent());
		jobStorage.remove(job.get().getId());
		Assert.assertFalse(jobStorage.iterator().hasNext());
		clientStorage.delete("my-client");
		Assert.assertTrue(clientStorage.getAll().isEmpty());
	}
}
