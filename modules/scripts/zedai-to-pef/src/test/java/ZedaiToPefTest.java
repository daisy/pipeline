import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.options;
import org.ops4j.pax.exam.Option;

public class ZedaiToPefTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("xml-to-pef"),
			brailleModule("common-utils"),
			brailleModule("liblouis-utils"),
			brailleModule("libhyphen-utils"),
			brailleModule("dotify-utils"),
			pipelineModule("css-utils"),
			brailleModule("css-utils"),
			brailleModule("pef-utils"),
			"org.daisy.pipeline.modules.braille:liblouis-utils:jar:" + thisPlatform() + ":?",
			pipelineModule("file-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("zedai-utils"),
			pipelineModule("zedai-to-epub3"),
			"org.daisy.pipeline:logging-activator:?",
		};
	}
	
	@Override @Configuration
	public Option[] config() {
		return options(
			// FIXME: BrailleUtils needs older version of jing
			mavenBundle("org.daisy.libs:jing:20120724.0.0"),
			composite(super.config()));
	}
}
