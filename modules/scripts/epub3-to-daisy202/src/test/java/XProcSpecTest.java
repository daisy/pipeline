import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.mavenBundle;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.options;
import org.ops4j.pax.exam.Option;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("file-utils"),
			pipelineModule("html-utils"),
			pipelineModule("mediatype-utils"),
			pipelineModule("epub-utils"),
			pipelineModule("smil-utils"),
			pipelineModule("daisy202-validator"),
		};
	}
	
	@Override @Configuration
	public Option[] config() {
		return options(
			// FIXME: epubcheck needs older version of jing
			mavenBundle("org.daisy.libs:jing:20120724.0.0"),
			composite(super.config()));
	}
	
	// XSpec tests are already run with Maven plugin
	@Override
	public void runXSpec() throws Exception {
	}
}
