import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			pipelineModule("css-utils"),
			pipelineModule("daisy3-utils"),
			pipelineModule("dtbook-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("file-utils"),
			pipelineModule("smil-utils"),
			pipelineModule("nlp-omnilang-lexer"),
			pipelineModule("tts-mocks"),
		};
	}
}
