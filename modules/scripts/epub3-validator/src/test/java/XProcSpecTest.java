import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.mavenBundle;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.options;
import org.ops4j.pax.exam.Option;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			pipelineModule("epubcheck-adapter")
		};
	}
	
	@Override @Configuration
	public Option[] config() {
		return options(
			// second version of guava needed for epubcheck-adapter
			mavenBundle("com.google.guava:guava:14.0.1"),
			composite(super.config()));
	}
}
