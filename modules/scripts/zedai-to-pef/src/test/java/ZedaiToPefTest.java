import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

public class ZedaiToPefTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("xml-to-pef"),
			brailleModule("common-utils"),
			brailleModule("liblouis-utils"),
			brailleModule("liblouis-tables"),
			brailleModule("libhyphen-core"),
			brailleModule("dotify-utils"),
			brailleModule("css-utils"),
			brailleModule("dotify-formatter"),
			brailleModule("pef-utils"),
			"org.daisy.pipeline.modules.braille:liblouis-native:jar:" + thisPlatform() + ":?",
			pipelineModule("file-utils"),
			pipelineModule("metadata-utils"),
			"org.daisy.pipeline:logging-activator:?",
			// FIXME: Dotify needs older version of jing
			"org.daisy.libs:jing:20120724.0.0",
		};
	}
}
