import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("common-utils"),
			brailleModule("css-utils"),
			brailleModule("pef-utils"),
			pipelineModule("common-utils"),
			pipelineModule("file-utils"),
			pipelineModule("fileset-utils"),
			// FIXME: pef-utils needs older version of jing
			"org.daisy.libs:jing:20120724.0.0",
		};
	}
}
