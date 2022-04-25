import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.common.HyphenatorRegistry;
import org.daisy.pipeline.braille.css.CSSStyledText;

import org.daisy.pipeline.junit.AbstractTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;

public class TexHyphenatorCoreTest extends AbstractTest {
	
	@Inject
	public HyphenatorRegistry provider;
	
	@Test
	public void testHyphenate() {
		assertEquals(text("foo\u00ADbar"),
		             provider.get(query("(table:'foobar.tex')")).iterator().next()
		                     .asFullHyphenator()
		                     .transform(styledText("foobar", "hyphens: auto")));
		assertEquals(text("foo-\u200Bbar"),
		             provider.get(query("(table:'foobar.tex')")).iterator().next()
		                     .asFullHyphenator()
		                     .transform(styledText("foo-bar", "hyphens: auto")));
		assertEquals(text("foo\u00ADbar"),
		             provider.get(query("(table:'foobar.properties')")).iterator().next()
		                     .asFullHyphenator()
		                     .transform(styledText("foobar", "hyphens: auto")));
		assertEquals(text("foo-\u200Bbar"),
		             provider.get(query("(table:'foobar.properties')")).iterator().next()
		                     .asFullHyphenator()
		                     .transform(styledText("foo-bar", "hyphens: auto")));
	}

	private Iterable<CSSStyledText> styledText(String... textAndStyle) {
		List<CSSStyledText> styledText = new ArrayList<CSSStyledText>();
		String text = null;
		boolean textSet = false;
		for (String s : textAndStyle) {
			if (textSet)
				styledText.add(new CSSStyledText(text, s));
			else
				text = s;
			textSet = !textSet; }
		if (textSet)
			throw new RuntimeException();
		return styledText;
	}

	private Iterable<CSSStyledText> text(String... text) {
		List<CSSStyledText> styledText = new ArrayList<CSSStyledText>();
		for (String t : text)
			styledText.add(new CSSStyledText(t, ""));
		return styledText;
	}

	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			brailleModule("braille-common"),
			"com.googlecode.texhyphj:texhyphj:?",
			"org.daisy.dotify:dotify.library:?",
			"org.daisy.libs:saxon-he:?",
			"org.daisy.pipeline:calabash-adapter:?"
		};
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		// needed because it can not be generated with maven-bundle-plugin
		probe.setHeader("Service-Component", "OSGI-INF/table-path.xml");
		return probe;
	}
}
