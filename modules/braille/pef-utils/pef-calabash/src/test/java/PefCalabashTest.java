import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class PefCalabashTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("common-utils"),
			brailleModule("css-core"),
			brailleModule("pef-core"),
			"org.daisy.braille:braille-utils.impl:?",
			"org.daisy.braille:braille-utils.pef-tools:?",
			"org.daisy.pipeline:calabash-adapter:?",
			"org.daisy.pipeline:logging-activator:?",
			// FIXME: BrailleUtils needs older version of jing
			"org.daisy.libs:jing:20120724.0.0",
		};
	}
}
