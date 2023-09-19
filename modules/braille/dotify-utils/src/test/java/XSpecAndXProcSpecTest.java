import javax.inject.Inject;

import org.daisy.dotify.api.text.Integer2TextFactoryMakerService;

import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

import org.junit.Test;
import org.junit.Assert;

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
			brailleModule("braille-css-utils"),
			brailleModule("pef-utils"),
			brailleModule("obfl-utils"),
			brailleModule("braille-common"),
			brailleModule("liblouis-utils"),
			"org.daisy.pipeline.modules.braille:liblouis-utils:jar:" + thisPlatform() + ":?",
			brailleModule("libhyphen-utils"),
			"org.daisy.pipeline.modules.braille:libhyphen-utils:jar:" + thisPlatform() + ":?",
			pipelineModule("css-utils"),
			"org.daisy.dotify:dotify.library:?",
			"com.google.guava:guava:?",
			"org.daisy.pipeline:calabash-adapter:?",
			// because the exclusion of com.fasterxml.woodstox:woodstox-core from the dotify.library
			// dependencies causes stax2-api to be excluded too
			"org.codehaus.woodstox:stax2-api:jar:?",
		};
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		// needed because it can not be generated with maven-bundle-plugin
		probe.setHeader("Service-Component", "OSGI-INF/number-braille-translator-provider.xml");
		return probe;
	}
}
