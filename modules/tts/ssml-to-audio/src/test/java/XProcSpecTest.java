import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("tts-common"),
			pipelineModule("tts-helpers"),
			pipelineModule("nlp-omnilang-lexer"),
			"org.daisy.pipeline:common-utils:?",
			"org.daisy.pipeline:calabash-adapter:?",
			"org.daisy.libs:saxon-he:?",
		};
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		// FIXME: can not delete this yet because it can not be generated with maven-bundle-plugin
		probe.setHeader("Service-Component", "OSGI-INF/mock-tts.xml,OSGI-INF/mock-encoder.xml");
		return probe;
	}
}
