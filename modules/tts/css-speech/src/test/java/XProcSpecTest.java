import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			"org.daisy.libs:jstyleparser:?",
			"commons-io:commons-io:?",
			"org.daisy.pipeline:calabash-adapter:?",
			"org.daisy.pipeline.modules:tts-helpers:?",
		};
	}
}
