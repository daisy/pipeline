import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			"org.daisy.pipeline:calabash-adapter:?",
			pipelineModule("common-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("file-utils"),
		};
	}
}
