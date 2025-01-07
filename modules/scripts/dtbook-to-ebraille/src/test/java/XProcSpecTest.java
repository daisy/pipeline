import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {

	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("file-utils"),
			pipelineModule("css-utils"),
			pipelineModule("dtbook-utils"),
			pipelineModule("epub-utils"),
			pipelineModule("dtbook-to-epub3"),
			brailleModule("braille-common"),
			brailleModule("braille-css-utils"),
			brailleModule("liblouis-utils"),
			brailleModule("dtbook-to-pef"),
			"org.daisy.pipeline.modules.braille:liblouis-utils:jar:" + thisPlatform() + ":?",
			"org.daisy.pipeline.modules.braille:libhyphen-utils:jar:" + thisPlatform() + ":?",
		};
	}
}
