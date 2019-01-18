import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.options;
import org.ops4j.pax.exam.Option;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("xml-to-pef"),
			brailleModule("common-utils"),
			brailleModule("pef-utils"),
			brailleModule("liblouis-utils"),
			brailleModule("dotify-utils"),
			brailleModule("liblouis-tables"),
			"org.daisy.pipeline.modules.braille:liblouis-native:jar:" + thisPlatform() + ":?",
			brailleModule("dotify-formatter"),
			pipelineModule("file-utils"),
			pipelineModule("html-utils"),
			pipelineModule("common-utils"),
			"org.daisy.pipeline:logging-activator:?",
			"org.daisy.pipeline:logging-appender:?",
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
