import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import org.daisy.pipeline.junit.AbstractTest;
import org.daisy.pipeline.junit.TestConfiguration;

import org.ops4j.pax.exam.util.PathUtils;

public abstract class TestBase extends AbstractTest {
	
	private static final File PIPELINE_BASE = new File(new File(PathUtils.getBaseDir()), "target/tmp");
	protected static final File PIPELINE_DATA = new File(PIPELINE_BASE, "data");
	
	// Apparently deleting the database folder and creating a new EntityManagerFactory is not enough
	// to start with a clean database. We can also not use a different base folder for every test
	// class because we do not handle changes in system properties. We therefore set reuseForks to
	// false in the Surefire configuration (so that each test class is run in a new process).
	@TestConfiguration
	public void setup() {
		try {
			FileUtils.deleteDirectory(PIPELINE_BASE);
			new File(PIPELINE_BASE, "log").mkdirs();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected Properties systemProperties() {
		Properties props = new Properties();
		props.setProperty("org.daisy.pipeline.data", PIPELINE_DATA.getAbsolutePath());
		props.setProperty("org.daisy.pipeline.logdir", new File(PIPELINE_BASE, "log").getAbsolutePath());
		return props;
	}
}
