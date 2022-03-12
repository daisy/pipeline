import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;

public class XSpecAndXProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			"org.daisy.braille:braille-css:?",
			"org.daisy.dotify:dotify.library:?",
			"org.daisy.pipeline:calabash-adapter:?",
			pipelineModule("common-utils"),
			pipelineModule("css-utils"),
			brailleModule("braille-css-utils"),
			// FIXME: make this dotify.library dependency optional
			"org.liblouis:liblouis-java:4.3.1",
			// because the exclusion of com.fasterxml.woodstox:woodstox-core from the dotify.library
			// dependencies causes stax2-api to be excluded too
			"org.codehaus.woodstox:stax2-api:jar:?",
		};
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		// needed because it can not be generated with maven-bundle-plugin
		probe.setHeader("Service-Component", "OSGI-INF/uppercase-transform-provider.xml");
		return probe;
	}
}
