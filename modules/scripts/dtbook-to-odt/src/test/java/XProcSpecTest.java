import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("asciimath-utils"),
			pipelineModule("dtbook-utils"),
			pipelineModule("file-utils"),
			pipelineModule("odt-utils")
		};
	}
}
