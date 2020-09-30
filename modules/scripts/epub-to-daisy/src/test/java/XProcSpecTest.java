import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("epub-utils"),
			pipelineModule("epub2-to-epub3"),
			pipelineModule("epub3-to-epub3"),
			pipelineModule("epub3-to-daisy202"),
			pipelineModule("epub3-to-daisy3"),
			pipelineModule("nlp-omnilang-lexer"),
			pipelineModule("tts-common"),
			pipelineModule("audio-common"),
		};
	}

	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		// FIXME: can not delete this yet because it can not be generated with maven-bundle-plugin
		probe.setHeader("Service-Component", "OSGI-INF/mock-tts.xml,OSGI-INF/mock-encoder.xml");
		return probe;
	}
}
