import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			pipelineModule("css-speech"),
			pipelineModule("dtbook-to-zedai"),
			pipelineModule("dtbook-utils"),
			pipelineModule("epub3-ocf-utils"),
			pipelineModule("file-utils"),
			pipelineModule("zedai-to-epub3"),
			pipelineModule("common-entities"),
			pipelineModule("nlp-omnilang-lexer"),
			pipelineModule("audio-encoder-lame"),
			pipelineModule("tts-common"),
		};
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		// FIXME: can not delete this yet because it can not be generated with maven-bundle-plugin
		probe.setHeader("Service-Component", "OSGI-INF/mock-tts.xml");
		return probe;
	}
}
