import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class TexHyphenatorSaxonTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("texhyph-core"),
			"org.daisy.pipeline:calabash-adapter:?"
		};
	}
}
