import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("fileset-utils"),
			"org.daisy.libs:jstyleparser:?",
			"commons-io:commons-io:?",
			"org.daisy.pipeline:calabash-adapter:?",
			"org.daisy.pipeline.modules:tts-common:?",
		};
	}
}
