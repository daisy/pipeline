import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("common-utils"),
			brailleModule("css-utils"),
			brailleModule("liblouis-utils"),
			"org.daisy.pipeline.modules.braille:liblouis-utils:jar:" + thisPlatform() + ":?",
			"org.daisy.pipeline.modules.braille:libhyphen-utils:jar:" + thisPlatform() + ":?",
			pipelineModule("file-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("common-utils"),
			pipelineModule("html-utils"),
			pipelineModule("epub-utils"),
			pipelineModule("epub3-tts"),
			pipelineModule("nlp-omnilang-lexer"),
			pipelineModule("tts-common"),
			pipelineModule("audio-common"),
			"org.daisy.pipeline:calabash-adapter:?"
		};
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		// FIXME: can not delete this yet because it can not be generated with maven-bundle-plugin
		probe.setHeader("Service-Component", "OSGI-INF/mock-tts.xml,OSGI-INF/mock-encoder.xml");
		return probe;
	}
}
