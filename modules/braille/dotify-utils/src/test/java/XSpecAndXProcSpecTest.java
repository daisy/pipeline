import javax.inject.Inject;

import org.daisy.dotify.api.text.Integer2TextFactoryMakerService;

import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

import org.junit.Test;
import org.junit.Assert;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.options;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;

public class XSpecAndXProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Inject
	public Integer2TextFactoryMakerService int2textFactory;
	
	@Test
	public void testInt2textFactory() throws Exception {
		Assert.assertEquals("tolv", int2textFactory.newInteger2Text("sv-SE").intToText(12));
	}
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("css-utils"),
			brailleModule("pef-utils"),
			brailleModule("obfl-utils"),
			brailleModule("common-utils"),
			brailleModule("liblouis-utils"),
			"org.daisy.pipeline.modules.braille:liblouis-utils:jar:" + thisPlatform() + ":?",
			brailleModule("libhyphen-utils"),
			"org.daisy.pipeline.modules.braille:libhyphen-utils:jar:" + thisPlatform() + ":?",
			pipelineModule("css-utils"),
			"com.google.guava:guava:?",
			"org.daisy.pipeline:calabash-adapter:?",
			"org.slf4j:jul-to-slf4j:?",
			"org.daisy.pipeline:logging-activator:?",
			"org.daisy.pipeline:logging-appender:?",
			// because the exclusion of com.fasterxml.woodstox:woodstox-core from the dotify.library
			// dependencies causes stax2-api to be excluded too
			"org.codehaus.woodstox:stax2-api:jar:?",
		};
	}
	
	@Override @Configuration
	public Option[] config() {
		return options(
			// apparently the liblouis-java exclusion defined in modules-bom does not have an effect
			mavenBundle("org.daisy.dotify:dotify.library:?"),
			composite(super.config()));
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		probe.setHeader("Service-Component", "OSGI-INF/number-braille-translator-provider.xml");
		return probe;
	}
}
