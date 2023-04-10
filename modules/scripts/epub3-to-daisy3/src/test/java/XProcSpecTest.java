import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {

	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("html-to-dtbook"),
			pipelineModule("epub3-to-html"),
			pipelineModule("epub-utils"),
			pipelineModule("html-utils"),
			pipelineModule("dtbook-utils"),
			pipelineModule("daisy3-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("common-utils"),
		};
	}
}
