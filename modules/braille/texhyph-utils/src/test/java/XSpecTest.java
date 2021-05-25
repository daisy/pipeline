import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			brailleModule("common-utils"),
			"com.googlecode.texhyphj:texhyphj:?",
			"org.daisy.dotify:dotify.library:?",
			"org.daisy.libs:saxon-he:?",
			"org.daisy.pipeline:calabash-adapter:?",
		};
	}
}
