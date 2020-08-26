import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.mavenBundle;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.options;
import org.ops4j.pax.exam.Option;

public class XSpecAndXProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			pipelineModule("file-utils"),
			pipelineModule("html-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("smil-utils"),
			pipelineModule("mediatype-utils"),
			pipelineModule("zip-utils"),
			pipelineModule("odf-utils"),
			pipelineModule("daisy3-utils"),
			pipelineModule("epubcheck-adapter"),
			pipelineModule("ace-adapter"),
		};
	}
	
	@Override @Configuration
	public Option[] config() {
		return options(
			// FIXME: epubcheck needs older version of jing
			mavenBundle("org.daisy.libs:jing:20120724.0.0"),
			composite(super.config()));
	}
}
