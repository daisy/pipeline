import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;

public class PaxExamConfig extends AbstractXSpecAndXProcSpecTest {
	
	public static boolean onWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			"org.daisy.libs:io.bit3.jsass:?",
			"com.google.guava:guava:?",
			// FIXME: skipped because it's also a dependency of calabash-adapter and because
			// currently there is no other way to exclude log4j-slf4j-impl than via calabash-adapter
			// "org.daisy.libs:com.xmlcalabash:?",
			"org.daisy.libs:saxon-he:?",
			"org.daisy.libs:jstyleparser:?",
			"org.daisy.pipeline:calabash-adapter:?",
			"org.daisy.pipeline.modules:common-utils:?",
			"org.daisy.pipeline.modules:file-utils:?",
			brailleModule("common-utils"),
			"org.daisy.pipeline:logging-appender:?",
		};
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		probe.setHeader("Bundle-Name", "test-module");
		// FIXME: can not delete this yet because it can not be generated with maven-bundle-plugin
		probe.setHeader("Service-Component", "OSGI-INF/module.xml");
		return probe;
	}
}
