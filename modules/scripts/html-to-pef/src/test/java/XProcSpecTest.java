import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("common-utils"),
			brailleModule("pef-utils"),
			brailleModule("liblouis-utils"),
			brailleModule("dotify-utils"),
			"org.daisy.pipeline.modules.braille:liblouis-utils:jar:" + thisPlatform() + ":?",
			pipelineModule("file-utils"),
			pipelineModule("html-utils"),
			pipelineModule("common-utils"),
			pipelineModule("html-to-epub3"),
			"org.daisy.pipeline:logging-activator:?",
			"org.daisy.pipeline:logging-appender:?",
		};
	}
}
