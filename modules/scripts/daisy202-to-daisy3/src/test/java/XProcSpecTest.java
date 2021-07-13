import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			pipelineModule("file-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("html-utils"),
			pipelineModule("smil-utils"),
			pipelineModule("daisy202-utils"),
			pipelineModule("daisy3-utils"),
			pipelineModule("dtbook-utils"),
			pipelineModule("html-to-dtbook"),
		};
	}
}
