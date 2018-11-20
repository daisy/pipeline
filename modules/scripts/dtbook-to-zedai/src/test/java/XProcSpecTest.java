import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			pipelineModule("css-speech"),
			pipelineModule("dtbook-utils"),
			pipelineModule("dtbook-validator"),
			pipelineModule("fileset-utils"),
			pipelineModule("file-utils"),
			pipelineModule("mediatype-utils"),
			pipelineModule("metadata-utils"),
			pipelineModule("validation-utils"),
			pipelineModule("common-entities"),
		};
	}
	
	// XSpec tests are already run with Maven plugin
	@Override
	public void runXSpec() throws Exception {
	}
}
