import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			"org.daisy.pipeline:calabash-adapter:?",
			"org.daisy.pipeline:logging-activator:?",
			brailleModule("common-utils"),
			brailleModule("css-core"),
			brailleModule("liblouis-core"),
			brailleModule("libhyphen-core"),
			brailleModule("xml-to-pef"),
			brailleModule("pef-utils"),
			brailleModule("css-utils"),
			brailleModule("liblouis-utils"),
			brailleModule("liblouis-tables"),
			brailleModule("libhyphen-libreoffice-tables"),
			brailleModule("dtbook-to-pef"),
			brailleModule("epub3-to-pef"),
			brailleModule("dotify-formatter"),
			brailleModule("html-to-pef"),
			"org.daisy.pipeline.modules.braille:liblouis-native:jar:" + thisPlatform() + ":?",
			"org.daisy.pipeline.modules.braille:libhyphen-native:jar:" + thisPlatform() + ":?",
			pipelineModule("file-utils"),
			// FIXME: Dotify needs older version of jing
			"org.daisy.libs:jing:20120724.0.0",
		};
	}
}

