import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			pipelineModule("css-speech"),
			pipelineModule("dtbook-to-zedai"),
			pipelineModule("dtbook-utils"),
			pipelineModule("epub-utils"),
			pipelineModule("file-utils"),
			pipelineModule("zedai-to-epub3"),
			pipelineModule("nlp-omnilang-lexer"),
			pipelineModule("audio-common"),
			pipelineModule("tts-mocks"),
		};
	}
}
