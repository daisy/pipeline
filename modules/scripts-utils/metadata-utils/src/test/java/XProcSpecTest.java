import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			"org.daisy.libs:jing:?",
			pipelineModule("common-utils"),
			pipelineModule("validation-utils"),
		};
	}
}
