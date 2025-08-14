import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("file-utils"),
			pipelineModule("mediatype-utils"),
			pipelineModule("validation-utils"),
			pipelineModule("css-utils"),
			pipelineModule("metadata-utils"),
			pipelineModule("mathml-utils"),
			pipelineModule("nlp-common"),
		};
	}
}

