import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			"org.daisy.pipeline:common-utils:?",
			"nu.validator.htmlparser:htmlparser:?",
			pipelineModule("common-utils"),
			pipelineModule("file-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("mediatype-utils"),
			pipelineModule("css-utils"),
			pipelineModule("nlp-common"),
			pipelineModule("nlp-omnilang-lexer"),
		};
	}
}
