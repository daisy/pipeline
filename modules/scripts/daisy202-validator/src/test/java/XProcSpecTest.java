import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			pipelineModule("daisy202-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("smil-utils"),
			pipelineModule("mediatype-utils"),
			pipelineModule("validation-utils"),
		};
	}
}
