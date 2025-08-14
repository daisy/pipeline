import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("braille-common"),
			brailleModule("braille-css-utils"),
			brailleModule("css-utils"),
			brailleModule("liblouis-utils"),
			"org.daisy.pipeline.modules.braille:liblouis-utils:jar:" + thisPlatform() + ":?",
			"org.daisy.pipeline.modules.braille:libhyphen-utils:jar:" + thisPlatform() + ":?",
			pipelineModule("file-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("common-utils"),
			pipelineModule("html-utils"),
			pipelineModule("epub-utils"),
			pipelineModule("nlp-omnilang-lexer"),
			pipelineModule("tts-common"),
			pipelineModule("tts-mocks"),
			pipelineModule("audio-common"),
			"org.daisy.pipeline:calabash-adapter:?"
		};
	}
}
