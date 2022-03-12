import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

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
			pipelineModule("tts-mocks"),
			pipelineModule("audio-common"),
		};
	}
}
