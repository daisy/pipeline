import javax.inject.Inject;

import static org.daisy.pipeline.braille.common.Query.util.query;

import org.daisy.pipeline.junit.AbstractTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;

public class TexHyphenatorCoreTest extends AbstractTest {
	
	@Inject
	public DispatchingTexHyphenatorProvider provider;
	
	@Test
	public void testHyphenate() {
		assertEquals("foo\u00ADbar",
		             provider.get(query("(table:'foobar.tex')")).iterator().next()
		                     .asFullHyphenator()
		                     .transform("foobar"));
		assertEquals("foo-\u200Bbar",
		             provider.get(query("(table:'foobar.tex')")).iterator().next()
		                     .asFullHyphenator()
		                     .transform("foo-bar"));
		assertEquals("foo\u00ADbar",
		             provider.get(query("(table:'foobar.properties')")).iterator().next()
		                     .asFullHyphenator()
		                     .transform("foobar"));
		assertEquals("foo-\u200Bbar",
		             provider.get(query("(table:'foobar.properties')")).iterator().next()
		                     .asFullHyphenator()
		                     .transform("foo-bar"));
	}
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			brailleModule("common-utils"),
			"com.googlecode.texhyphj:texhyphj:?",
			"org.daisy.dotify:dotify.library:?",
			"org.daisy.libs:saxon-he:?",
			"org.daisy.pipeline:calabash-adapter:?"
		};
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		probe.setHeader("Service-Component", "OSGI-INF/table-path.xml," +
		                                     "OSGI-INF/dispatching-tex-hyphenator-provider.xml");
		return probe;
	}
}
