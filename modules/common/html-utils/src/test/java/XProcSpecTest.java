import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.mavenBundlesWithDependencies;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.options;
import org.ops4j.pax.exam.Option;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	/*@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("file-utils"),
			pipelineModule("fileset-utils"),
		};
	}*/
	
	@Override @Configuration
	public Option[] config() {
		return options(
			composite(super.config()),
			mavenBundlesWithDependencies(
				mavenBundle("org.daisy.pipeline.modules:file-utils:?"),
				// need to exclude html-utils explicitly because it won't be detected automatically
				mavenBundle("org.daisy.pipeline.modules:fileset-utils:?")
					.exclusion("org.daisy.pipeline.modules", "html-utils")));
	}
}
