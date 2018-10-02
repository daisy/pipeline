import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			pipelineModule("css-speech"),
			pipelineModule("css-utils"),
			pipelineModule("daisy3-utils"),
			pipelineModule("dtbook-tts"),
			pipelineModule("dtbook-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("file-utils"),
			pipelineModule("tts-helpers"),
			pipelineModule("common-entities"),
			pipelineModule("nlp-omnilang-lexer"),
			pipelineModule("audio-encoder-lame"),
			pipelineModule("tts-common"),
		};
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		probe.setHeader("Service-Component", "OSGI-INF/mock-tts.xml");
		return probe;
	}
}
