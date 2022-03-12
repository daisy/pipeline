import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

public class ZedaiToPefTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("braille-common"),
			brailleModule("liblouis-utils"),
			brailleModule("libhyphen-utils"),
			brailleModule("dotify-utils"),
			pipelineModule("css-utils"),
			brailleModule("braille-css-utils"),
			brailleModule("pef-utils"),
			"org.daisy.pipeline.modules.braille:liblouis-utils:jar:" + thisPlatform() + ":?",
			pipelineModule("common-utils"),
			pipelineModule("file-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("zedai-utils"),
			pipelineModule("zedai-to-epub3"),
		};
	}
}
