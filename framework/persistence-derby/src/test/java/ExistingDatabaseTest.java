import java.io.File;
import java.io.IOException;
import javax.inject.Inject;

import com.google.common.base.Optional;

import org.daisy.pipeline.junit.OSGiLessConfiguration;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.ClientStorage;
import org.daisy.pipeline.webserviceutils.storage.WebserviceStorage;

import org.junit.Assert;
import org.junit.Test;

import org.apache.commons.io.FileUtils;

import org.ops4j.pax.exam.util.PathUtils;

public class ExistingDatabaseTest extends TestBase {
	
	// copy existing database to PIPELINE_DATA
	@Override @OSGiLessConfiguration
	public void setup() {
		super.setup();
		File existingData = new File(new File(PathUtils.getBaseDir()), "src/test/resources/data");
		try {
			FileUtils.copyDirectory(existingData, PIPELINE_DATA);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Inject
	public WebserviceStorage webserviceStorage;
	
	@Test
	public void testClientStorage() {
		ClientStorage clientStorage = webserviceStorage.getClientStorage();
		Optional<Client> client = clientStorage.get("my-client");
		Assert.assertTrue(client.isPresent());
		Assert.assertEquals("me@daisy.org", client.get().getContactInfo());
	}
}
