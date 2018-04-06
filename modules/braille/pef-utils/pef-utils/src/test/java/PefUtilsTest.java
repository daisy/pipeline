import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class PefUtilsTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("pef-calabash"),
			brailleModule("pef-saxon"),
			pipelineModule("file-utils"),
			"org.daisy.pipeline:logging-activator:?",
			// FIXME: BrailleUtils needs older version of jing
			"org.daisy.libs:jing:20120724.0.0",
		};
	}
}
