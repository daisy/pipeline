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
			pipelineModule("css-speech"),
			pipelineModule("dtbook-to-zedai"),
			pipelineModule("dtbook-utils"),
			pipelineModule("epub3-ocf-utils"),
			pipelineModule("file-utils"),
			pipelineModule("zedai-to-epub3")
		};
	}
	
	@Override @Configuration
	public Option[] config() {
		return options(
			composite(super.config()),
			
			// for org.apache.httpcomponents:httpclient (<-- xmlcalabash):
			mavenBundle("org.slf4j:jcl-over-slf4j:1.7.2")
		);
	}
}

