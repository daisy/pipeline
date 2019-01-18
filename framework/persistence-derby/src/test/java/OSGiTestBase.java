import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import org.daisy.pipeline.junit.AbstractTest;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.util.PathUtils;

public abstract class OSGiTestBase extends AbstractTest {
	
	@Override
	public String[] testDependencies() {
		return new String[]{
			"org.daisy.pipeline:framework-persistence:?",
		};
	}
	
	private static final File PIPELINE_BASE = new File(new File(PathUtils.getBaseDir()), "target/tmp");
	protected static final File PIPELINE_DATA = new File(PIPELINE_BASE, "data");
	
	protected void setup() {
		try {
			FileUtils.deleteDirectory(PIPELINE_BASE);
			new File(PIPELINE_BASE, "log").mkdirs();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// Note that "Predeployment of PersistenceUnit [pipeline-pu] failed" errors can be ignored
	@Override @Configuration
	public Option[] config() {
		
		// framework started once per class, so initialize once per class
		// calling this code from config() is the only way to achieve this (@BeforeClass does not
		// work, nor does a static {} block)
		setup();
		return options(
			composite(super.config()),
			systemProperty("org.daisy.pipeline.iobase").value(new File(PIPELINE_DATA, "jobs").getAbsolutePath()),
			systemProperty("org.daisy.pipeline.data").value(PIPELINE_DATA.getAbsolutePath()),
			systemProperty("derby.stream.error.file").value(new File(PIPELINE_BASE, "log/derby.log").getAbsolutePath()),
			mavenBundle("org.eclipse:org.eclipse.gemini.jpa:?"),
			mavenBundle("org.eclipse.gemini:org.eclipse.gemini.dbaccess.derby:?"),
			mavenBundle("org.eclipse.gemini:org.eclipse.gemini.dbaccess.util:?"),
			mavenBundle("org.eclipse.persistence:org.eclipse.persistence.asm:?"),
			mavenBundle("org.eclipse.persistence:org.eclipse.persistence.antlr:?"),
			mavenBundle("org.eclipse.persistence:org.eclipse.persistence.core:?"),
			mavenBundle("org.eclipse.persistence:org.eclipse.persistence.jpa:?"),
			mavenBundle("org.eclipse.persistence:javax.persistence:?"),
			mavenBundle("org.eclipse.gemini:org.apache.derby:?"),
			mavenBundle("org.osgi:org.osgi.enterprise:?")
		);
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		probe.setHeader("Bundle-Name", "Test module");
		probe.setHeader("Service-Component", "OSGI-INF/script.xml");
		return probe;
	}
}
