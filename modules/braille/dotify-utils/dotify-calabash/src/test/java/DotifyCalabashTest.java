import javax.inject.Inject;

import org.daisy.dotify.api.text.Integer2TextFactoryMakerService;

import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

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
			brailleModule("common-utils"),
			brailleModule("obfl-utils"),
			brailleModule("pef-utils"),
			// logging
			"org.slf4j:jul-to-slf4j:?",
			"org.daisy.pipeline:logging-activator:?"
		};
	}
}
