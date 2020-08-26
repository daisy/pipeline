import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			pipelineModule("html-utils"),
			pipelineModule("daisy3-utils"),
			pipelineModule("dtbook-to-html"),
			pipelineModule("epub-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("file-utils"),
			pipelineModule("smil-utils"),
		};
	}
}

