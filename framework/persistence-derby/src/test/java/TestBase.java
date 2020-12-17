import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import org.daisy.pipeline.junit.AbstractTest;
import org.daisy.pipeline.junit.OSGiLessConfiguration;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.util.PathUtils;

// The following errors (present when run with PaxExam) can be ignored:
// - Predeployment of PersistenceUnit [pipeline-pu] failed
// - No Persistence provider for EntityManager named pipeline-pu

public abstract class TestBase extends AbstractTest {
	
	@Override
	public String[] testDependencies() {
		return new String[]{
			"org.daisy.pipeline:framework-persistence:?",
			"org.eclipse.gemini:org.apache.derby:?",
			"org.eclipse.gemini:org.eclipse.gemini.dbaccess.derby:?",
			"org.eclipse.gemini:org.eclipse.gemini.dbaccess.util:?",
			"org.eclipse.persistence:javax.persistence:?",
			"org.eclipse.persistence:org.eclipse.persistence.antlr:?",
			"org.eclipse.persistence:org.eclipse.persistence.asm:?",
			"org.eclipse.persistence:org.eclipse.persistence.core:?",
			"org.eclipse.persistence:org.eclipse.persistence.jpa:?",
			"org.eclipse:org.eclipse.gemini.jpa:?",
		};
	}
	
	private static final File PIPELINE_BASE = new File(new File(PathUtils.getBaseDir()), "target/tmp");
	protected static final File PIPELINE_DATA = new File(PIPELINE_BASE, "data");
	
	// Apparently deleting the database folder and creating a new EntityManagerFactory is not enough
	// to start with a clean database. We can also not use a different base folder for every test
	// class because we do not handle changes in system properties. We therefore set reuseForks to
	// false in the Surefire configuration (so that each test class is run in a new process).
	@OSGiLessConfiguration
	public void setup() {
		try {
			FileUtils.deleteDirectory(PIPELINE_BASE);
			new File(PIPELINE_BASE, "log").mkdirs();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override @Configuration
	public Option[] config() {
		setup();
		return super.config();
	}
	
	@Override
	protected Properties systemProperties() {
		Properties props = new Properties();
		props.setProperty("org.daisy.pipeline.data", PIPELINE_DATA.getAbsolutePath());
		props.setProperty("org.daisy.pipeline.logdir", new File(PIPELINE_BASE, "log").getAbsolutePath());
		return props;
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		// FIXME: can not delete this yet because it can not be generated with maven-bundle-plugin
		probe.setHeader("Service-Component", "OSGI-INF/script.xml");
		return probe;
	}
}
