import javax.inject.Inject;

import org.daisy.dotify.api.text.Integer2TextFactoryMakerService;

import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.mavenBundle;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.options;
import org.ops4j.pax.exam.Option;

public class DotifyCalabashTest extends AbstractXSpecAndXProcSpecTest {
	
	@Inject
	private Integer2TextFactoryMakerService int2textFactory;
	
	@Test
	public void testInt2textFactory() throws Exception {
		assertEquals("tolv", int2textFactory.newInteger2Text("sv-SE").intToText(12));
	}
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			// tasks
			"org.daisy.streamline:streamline-api:?",
			"org.daisy.streamline:streamline-engine:?",
			"org.daisy.dotify:dotify.api:?",
			"org.daisy.dotify:dotify.common:?",
			"org.daisy.dotify:dotify.task.impl:?",
			// formatter
			"org.daisy.dotify:dotify.formatter.impl:?",
			"org.daisy.dotify:dotify.text.impl:?",
			"org.daisy.dotify:dotify.hyphenator.impl:?",
			"org.daisy.dotify:dotify.translator.impl:?",
			// for the query syntax
			brailleModule("css-core"),
			// other
			"org.daisy.pipeline:calabash-adapter:?",
			"org.codehaus.woodstox:stax2-api:jar:?", // FIXME: because otherwise the exclusion of com.fasterxml.woodstox:woodstox-core
			                                         // from the dotify.formatter.impl dependencies would cause stax2-api to be excluded too
			brailleModule("common-utils"),
			brailleModule("obfl-utils"),
			brailleModule("pef-utils"),
			// logging
			"org.slf4j:jul-to-slf4j:?",
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
