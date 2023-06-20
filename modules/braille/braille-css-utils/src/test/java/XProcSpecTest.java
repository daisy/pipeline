import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {

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
			"org.daisy.pipeline.modules:css-utils:?",
			"org.daisy.pipeline.modules:file-utils:?",
		};
	}

	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		probe.setHeader("Bundle-Name", "test-module");
		// needed because it can not be generated with maven-bundle-plugin
		probe.setHeader("Service-Component", "OSGI-INF/module.xml");
		return probe;
	}
}
