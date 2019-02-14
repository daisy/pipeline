import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			pipelineModule("dtbook-validator"),
			pipelineModule("fileset-utils"),
			pipelineModule("validation-utils"),
			pipelineModule("common-entities"),
		};
	}
}
