import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			pipelineModule("file-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("mediatype-utils"),
			"org.daisy.libs:jstyleparser:?",
			"org.unbescape:unbescape:?",
			"org.daisy.libs:braille-css:?",
			"org.apache.servicemix.bundles:org.apache.servicemix.bundles.antlr-runtime:?",
			"org.sharegov:mjson:?",
			"org.daisy.libs:io.bit3.jsass:?",
			"commons-io:commons-io:?",
			"com.google.guava:guava:?",
			"org.daisy.pipeline:calabash-adapter:?",
		};
	}
}
