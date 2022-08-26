import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {

	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("nlp-common"),
			pipelineModule("nlp-omnilang-lexer"),
		};
	}
}
