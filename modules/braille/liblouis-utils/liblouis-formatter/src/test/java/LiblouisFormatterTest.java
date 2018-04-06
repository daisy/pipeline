import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

public class LiblouisFormatterTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("common-utils"),
			brailleModule("css-core"),
			"org.daisy.braille:braille-utils.api:?",
			brailleModule("pef-utils"),
			brailleModule("css-utils"),
			brailleModule("liblouis-utils"),
			"org.daisy.pipeline.modules.braille:liblouis-native:jar:" + thisPlatform() + ":?",
			pipelineModule("file-utils"),
			pipelineModule("fileset-utils"),
			brailleModule("liblouis-tables"),
			// FIXME: BrailleUtils needs older version of jing
			"org.daisy.libs:jing:20120724.0.0",
		};
	}
}
