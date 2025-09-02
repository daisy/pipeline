import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {

	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			pipelineModule("tts-common"),
			"org.daisy.pipeline:common-utils:?",
			"org.daisy.pipeline:calabash-adapter:?",
			"org.daisy.pipeline:saxon-adapter:?",
		};
	}
}
