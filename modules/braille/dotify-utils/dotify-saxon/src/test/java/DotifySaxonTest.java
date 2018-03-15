import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class DotifySaxonTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("common-utils"),
			brailleModule("dotify-core"),
			"org.daisy.dotify:dotify.translator.impl:?",
			"org.daisy.pipeline:calabash-adapter:?"
		};
	}
}
