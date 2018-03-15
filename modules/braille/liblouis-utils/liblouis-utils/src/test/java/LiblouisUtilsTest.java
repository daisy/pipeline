import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

public class LiblouisUtilsTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("liblouis-core"),
			brailleModule("common-utils"),
			brailleModule("css-core"),
			brailleModule("liblouis-saxon"),
			brailleModule("liblouis-calabash"),
			brailleModule("css-utils"),
			brailleModule("libhyphen-core"),
			brailleModule("liblouis-tables"),
			"org.daisy.pipeline.modules.braille:liblouis-native:jar:" + thisPlatform() + ":?",
			"org.daisy.pipeline.modules.braille:libhyphen-native:jar:" + thisPlatform() + ":?"
		};
	}
}
