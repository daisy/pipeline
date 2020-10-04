import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;

public class XSpecAndXProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			"org.daisy.braille:braille-css:?",
			"org.daisy.dotify:dotify.api:?",
			"org.daisy.pipeline:calabash-adapter:?",
			pipelineModule("common-utils"),
			pipelineModule("css-utils"),
		};
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		probe.setHeader("Service-Component", "OSGI-INF/uppercase-transform-provider.xml");
		return probe;
	}
}
