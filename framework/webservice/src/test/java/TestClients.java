import org.daisy.pipeline.webservice.jaxb.clients.Client;
import org.daisy.pipeline.webservice.jaxb.clients.Clients;
import org.daisy.pipeline.webservice.jaxb.clients.Priority;

import org.junit.Assert;
import org.junit.Test;

public class TestClients extends Base {
	
	private static Client TEST_CLIENT_ADMIN; static {
		TEST_CLIENT_ADMIN = new Client();
		TEST_CLIENT_ADMIN.setId("myadmin");
		TEST_CLIENT_ADMIN.setRole("ADMIN");
		TEST_CLIENT_ADMIN.setSecret("mysecret");
		TEST_CLIENT_ADMIN.setContact("admin@daisy.org");
		TEST_CLIENT_ADMIN.setPriority(Priority.HIGH);
	}
	
	private static Client TEST_CLIENT_APP; static {
		TEST_CLIENT_APP = new Client();
		TEST_CLIENT_APP.setId("myclient");
		TEST_CLIENT_APP.setRole("CLIENTAPP");
		TEST_CLIENT_APP.setSecret("shh");
		TEST_CLIENT_APP.setContact("client@daisy.org");
		TEST_CLIENT_APP.setPriority(Priority.LOW);
	}
	
	@Test
	public void testDefaultClientNotShown() throws Exception {
		logger.info("testDefaultClientNotShown IN");
		Clients clients = client().clients();
		Assert.assertEquals("Default client is not listed (but is there I swear!)", clients.getClient().size(), 0);
		logger.info("testDefaultClientNotShown OUT");
	}
	
	@Test
	public void testAddClient() throws Exception {
		logger.info("addClient IN");
		Client client = client().addClient(TEST_CLIENT_ADMIN);
		deleteAfterTest(client);
		compareClients(TEST_CLIENT_ADMIN, client);
		compareClients(TEST_CLIENT_ADMIN, client().clients().getClient().get(0));
		logger.info("addClient OUT");
	}
	
	@Test
	public void testGetClient() throws Exception {
		logger.info("GetClient IN");
		client().addClient(TEST_CLIENT_ADMIN);
		deleteAfterTest(TEST_CLIENT_ADMIN);
		Client client = client().client(TEST_CLIENT_ADMIN.getId());
		compareClients(TEST_CLIENT_ADMIN, client);
		compareClients(TEST_CLIENT_ADMIN, client().clients().getClient().get(0));
		logger.info("GetClient OUT");
	}
	
	@Test
	public void testDeleteClient() throws Exception {
		logger.info("DeleteClient IN");
		Client client = client().addClient(TEST_CLIENT_ADMIN);
		client().deleteClient(client.getId());
		try {
			client().client(TEST_CLIENT_ADMIN.getId());
			Assert.fail("Get client should throw an exception in here");
		} catch (Exception e) {
		}
		logger.info("DeleteClient OUT");
	}
	
	private void compareClients(Client exp, Client act) {
		Assert.assertEquals("Client id", exp.getId(), act.getId());
		Assert.assertEquals("Client secret", exp.getSecret(), act.getSecret());
		Assert.assertEquals("Client role", exp.getRole(), act.getRole());
		Assert.assertEquals("Client contact", exp.getContact(), act.getContact());
	}
	
	@Test
	public void testModifyClient() throws Exception {
		logger.info("testModifyClient IN");
		Client client = client().addClient(TEST_CLIENT_APP);
		deleteAfterTest(client);
		Client modified = new Client();
		modified.setId(TEST_CLIENT_APP.getId());
		modified.setSecret(TEST_CLIENT_APP.getSecret());
		modified.setRole("ADMIN");
		modified.setContact("other@daisy.org");
		modified.setPriority(TEST_CLIENT_APP.getPriority());
		Client fromServer = client().updateClient(modified);
		compareClients(fromServer, modified);
		logger.info("testModifyClient OUT");
	}
}
