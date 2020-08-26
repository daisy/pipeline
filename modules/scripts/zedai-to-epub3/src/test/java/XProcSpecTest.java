import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			pipelineModule("css-speech"),
			pipelineModule("epub-utils"),
			pipelineModule("html-to-epub3"),
			pipelineModule("fileset-utils"),
			pipelineModule("file-utils"),
			pipelineModule("zedai-to-html"),
			pipelineModule("zedai-utils")
		};
	}
}
