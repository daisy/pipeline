import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("common-utils"),
			brailleModule("css-utils"),
			brailleModule("liblouis-utils"),
			brailleModule("liblouis-tables"),
			"org.daisy.pipeline.modules.braille:liblouis-native:jar:" + thisPlatform() + ":?",
			pipelineModule("file-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("common-utils"),
			pipelineModule("zip-utils"),
			// for fileset-utils and zip-utils
			pipelineModule("html-utils"),
			pipelineModule("mediatype-utils"),
			"org.daisy.pipeline:calabash-adapter:?"
		};
	}
}
