import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

public class LibhyphenSaxonTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("libhyphen-core"),
			"org.daisy.pipeline.modules.braille:libhyphen-native:jar:" + thisPlatform() + ":?",
			"org.daisy.pipeline:calabash-adapter:?"
		};
	}
}
