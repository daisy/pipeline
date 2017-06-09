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
			"org.daisy.libs:com.xmlcalabash:?",
			"org.daisy.libs:saxon-he:?",
			"org.daisy.libs:jstyleparser:?",
			"org.daisy.pipeline:calabash-adapter:?",
			brailleModule("common-utils"),
			brailleModule("css-core")
		};
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		probe.setHeader("Bundle-Name", "css-utils test");
		return probe;
	}
}
