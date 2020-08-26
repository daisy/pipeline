import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			pipelineModule("css-speech"),
			pipelineModule("dtbook-utils"),
			pipelineModule("zedai-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("file-utils"),
			pipelineModule("mediatype-utils"),
			pipelineModule("validation-utils"),
		};
	}
	
	// XSpec tests are already run with Maven plugin
	@Override
	public void runXSpec() throws Exception {
	}
}
