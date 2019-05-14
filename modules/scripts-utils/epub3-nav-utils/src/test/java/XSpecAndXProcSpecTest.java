import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import org.junit.Test;

public class XSpecAndXProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			pipelineModule("file-utils"),
			pipelineModule("html-utils"),
		};
	}
	
	@Override @Test
	public void runXSpec() {
		// already run with xspec-maven-plugin
	}
}
