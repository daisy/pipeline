import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

public class DediconTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
                    brailleModule("common-utils"),
                    brailleModule("css-utils"),
                    brailleModule("liblouis-utils"),
                    brailleModule("liblouis-tables"),
                    "org.daisy.pipeline.modules.braille:liblouis-native:jar:" + thisPlatform() + ":?",
                    brailleModule("libhyphen-core"),
                    brailleModule("libhyphen-libreoffice-tables"),
                    "org.daisy.pipeline.modules.braille:libhyphen-native:jar:" + thisPlatform() + ":?",
                    brailleModule("dotify-formatter"),
                    brailleModule("pef-utils"),
                    brailleModule("dtbook-to-pef")
                };
	}
}
