import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("transpect"),
			pipelineModule("file-utils"),
            pipelineModule("fileset-utils"),
            pipelineModule("html-utils")
		};
	}
}
