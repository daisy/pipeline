import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("transpect"),
			pipelineModule("file-utils"),
			pipelineModule("html-utils"),
			pipelineModule("html-to-epub3"),
			pipelineModule("epub3-ocf-utils")
		};
	}
}
