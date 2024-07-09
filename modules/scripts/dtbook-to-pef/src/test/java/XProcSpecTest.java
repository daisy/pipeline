import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("braille-common"),
			brailleModule("pef-utils"),
			brailleModule("obfl-utils"),
			brailleModule("liblouis-utils"),
			brailleModule("libhyphen-utils"),
			brailleModule("dotify-utils"),
			"org.daisy.pipeline.modules.braille:liblouis-utils:jar:" + thisPlatform() + ":?",
			"org.daisy.pipeline.modules.braille:libhyphen-utils:jar:" + thisPlatform() + ":?",
			pipelineModule("common-utils"),
			pipelineModule("css-utils"),
			pipelineModule("file-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("metadata-utils"),
			pipelineModule("dtbook-utils"),
			pipelineModule("dtbook-to-epub3"),
		};
	}
}
